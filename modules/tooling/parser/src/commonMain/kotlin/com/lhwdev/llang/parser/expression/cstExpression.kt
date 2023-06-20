package com.lhwdev.llang.parser.expression

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.asInline
import com.lhwdev.llang.cst.structure.core.CstIdentifier
import com.lhwdev.llang.cst.structure.core.CstLeafNode
import com.lhwdev.llang.cst.structure.core.CstLeafNodeImpl
import com.lhwdev.llang.cst.structure.core.CstOperator
import com.lhwdev.llang.cst.structure.expression.CstExpression
import com.lhwdev.llang.cst.structure.expression.CstMemberAccess
import com.lhwdev.llang.cst.structure.expression.CstOperation
import com.lhwdev.llang.cst.structure.expression.CstTuple
import com.lhwdev.llang.cst.structure.util.CstSurround
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.leafNode
import com.lhwdev.llang.parser.node
import com.lhwdev.llang.parser.structuredNode
import com.lhwdev.llang.parsing.util.parseError
import com.lhwdev.llang.parsing.util.parseRequire
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.parseExpressionToken
import com.lhwdev.llang.tokenizer.source.eof


private sealed interface CstTempNode : CstNode {
	interface TempToExpression : CstTempNode {
		context(CstParseContext)
		fun tempToExpression(): CstExpression
	}
	
	/**
	 * Special node that is not accepted yet into main [CstParseContext.code].
	 * [token] should be accepted to be sound.
	 *
	 * @see com.lhwdev.llang.parser.CstCodeSource.acceptToken
	 */
	class Leaf(val token: Token) : CstTempNode, TempToExpression {
		/**
		 * All `tempTo???` functions will 'mount' virtual [CstTempNode]s into CstNodeTree.
		 */
		context(CstParseContext)
		override fun tempToExpression() = when(token.kind) {
			is TokenKinds.Identifier -> structuredNode(CstGetValue) {
				CstGetValue(
					leafNode(CstIdentifier) { CstIdentifier(code.acceptToken(token)) },
				)
			}
			
			is TokenKinds.NumberLiteral -> leafNode(CstNumberLiteral) {
				CstNumberLiteral(code.acceptToken(token))
			}
			
			is TokenKinds.Operator -> parseError("Unexpected operator ${token.kind}")
			else -> parseError("Unexpected token $token")
		}
		
		context(CstParseContext)
		fun tempToBinaryOperator() = when(token.kind) {
			is TokenKinds.Identifier,
			is TokenKinds.Operator,
			-> structuredNode(CstOperator.Binary) {
				CstOperator.Binary(token)
			}
			
			else -> parseError("Unexpected token $token")
		}
		
		context(CstParseContext)
		fun tempToUnaryOperator() = when(token.kind) {
			is TokenKinds.Operator -> structuredNode(CstOperator.Unary) {
				CstOperator.Unary(token)
			}
			
			else -> parseError("Unexpected token $token")
		}
	}
	
	class Binary(
		val lhs: TempToExpression,
		val operator: Leaf,
		val rhs: TempToExpression,
	) : CstTempNode, TempToExpression {
		context(CstParseContext)
		override fun tempToExpression() = structuredNode(CstOperation.Binary) {
			CstOperation.Binary(
				lhs.tempToExpression(),
				operator.tempToBinaryOperator(),
				rhs.tempToExpression(),
			)
		}
	}
	
	class UnaryPrefix(
		val operator: Leaf,
		val operand: TempToExpression,
	) : CstTempNode, TempToExpression {
		context(CstParseContext)
		override fun tempToExpression() = structuredNode(CstOperation.UnaryPrefix) {
			CstOperation.UnaryPrefix(operator.tempToUnaryOperator(), operand.tempToExpression())
		}
	}
	
	class UnaryPostfix(
		val operand: Leaf,
		val operator: TempToExpression,
	) : CstTempNode, TempToExpression {
		context(CstParseContext)
		override fun tempToExpression() = structuredNode(CstOperation.UnaryPostfix) {
			CstOperation.UnaryPostfix(operator.tempToExpression(), operand.tempToUnaryOperator())
		}
	}
}

private class NodeBuffer(context: CstParseContext) {
	private val code = context.code.cloneForRead()
	private var peekBuffer = null as CstTempNode.Leaf?
	
	fun pop(): CstTempNode.Leaf =
		peekBuffer.also { peekBuffer = null } ?: CstTempNode.Leaf(code.parseExpressionToken())
	
	fun peek(): CstTempNode.Leaf =
		peekBuffer ?: CstTempNode.Leaf(code.parseExpressionToken()).also { peekBuffer = it }
	
	fun endOfInput(): Boolean {
		if(code.eof) return true
		
		val next = peek().token.kind
		return when {
			next is TokenKinds.Operator.Group && !next.open -> true
			else -> false
		}
	}
	
	fun close() {
		code.close()
	}
}

/**
 * Considerations:
 * - end of statement; LineBreak-separated statements
 * - precedence(is already considered)
 * - determining 'is this expression vs binaryOps vs unaryOps' is easier than you thought
 *
 * ENSURE ALL ORDER OF CALL TO `code.acceptToken` IS CONSISTENT
 */
private class CstExpressionParser(private val context: CstParseContext) {
	var buffer = NodeBuffer(context)
	var stack = ArrayDeque<CstNode>()
	var head: CstNode = buffer.pop()
	
	private inline fun <R> child(block: CstExpressionParser.() -> R): R {
		val previousStack = stack
		val previousHead = head
		return try {
			stack = ArrayDeque()
			head = buffer.pop()
			block()
		} finally {
			stack = previousStack
			head = previousHead
		}
	}
	
	private fun revalidateBuffer() {
		buffer.close()
		buffer = NodeBuffer(context)
	}
	
	private fun CstParseContext.leaf(token: Token = buffer.pop().token): CstLeafNode =
		leafNode(CstLeafNode) {
			CstLeafNodeImpl(code.acceptToken(token))
		}
	
	fun push() {
		stack.addLast(head)
		stack.addLast(buffer.pop())
		head = buffer.pop()
	}
	
	fun CstParseContext.unaryPrefixOps() = structuredNode(CstOperation.UnaryPrefix) {
		val operator = head.toUnaryOperator()
		head = buffer.pop()
		val operand = expandHeadEagerForExpression()
		
		CstOperation.UnaryPrefix(
			operator = operator,
			operand = operand,
		)
	}.also { head = it }
	
	fun CstParseContext.unaryPostfixOps() = structuredNode(CstOperation.UnaryPostfix) {
		val operand = expandHeadEagerForExpression()
		val operator = buffer.pop().toUnaryOperator()
		
		CstOperation.UnaryPostfix(
			operand = operand,
			operator = operator,
		)
	}.also { head = it }
	
	fun CstParseContext.binaryOps() = structuredNode(CstOperation.Binary) {
		val operator = stack.removeLast()
		val lhs = stack.removeLast()
		val rhs = head
		
		CstOperation.Binary(
			lhs = lhs.toExpression(),
			operator = operator.toBinaryOperator(),
			rhs = rhs.toExpression(),
		)
	}.also { head = it }
	
	fun CstParseContext.accessOps() = node(info = null) {
		val parent = head.toExpression()
		val accessor = leafNode(CstLeafNode) {
			CstLeafNodeImpl(code.acceptToken(buffer.pop().token))
		}
		head = buffer.pop()
		val item = expandHeadEagerForExpression()
		parseRequire(accessor.token.kind == TokenKinds.Operator.Access.Dot) {
			"TODO: implement accessor for :: Metadata and ?. SafeDot"
		}
		
		structuredNode(CstMemberAccess.asInline()) {
			CstMemberAccess(
				parent = parent,
				item = item,
			)
		}
	}.also { head = it }
	
	fun CstParseContext.callOps() = node(info = null) {
		val function = head.toExpression()
		val arguments = cstTuple()
		revalidateBuffer() // cstTuple depends on original [code]; buffer remains stale without [revalidateBuffer]
		
		CstFunctionCall(function, arguments)
	}
	
	fun CstParseContext.groupOrTupleOps() = structuredNode(CstSurround.info()) {
		val open = leaf()
		parseRequire(open.token.kind == TokenKinds.Operator.Group.LeftParen) { "no left paren" }
		
		val content = node(info = null) {
			val content = child { runMain() }
			val next = buffer.pop()
			when(next.token.kind) {
				TokenKinds.Operator.Other.Comma -> {
					val contents = mutableListOf(content)
					while(true) {
						val otherContent = child { runMain() }
						contents += otherContent
						
						val comma = leaf()
						when(comma.token.kind) {
							TokenKinds.Operator.Other.Comma -> {
								// trailing comma available
								if(buffer.peek().token.kind == TokenKinds.Operator.Group.RightParen) {
									leaf()
									break
								}
							}
							
							TokenKinds.Operator.Group.RightParen -> break
						}
					}
					
					CstTuple(contents)
				}
				
				TokenKinds.Operator.Group.RightParen -> {
					leaf(next.token)
					content
				}
				
				else -> parseError("Unknown end of child expression parsing")
			}
		}
		CstSurround(kind = CstSurround.Paren, content)
	}.content
	
	fun CstParseContext.expandHeadEagerForExpression(): CstExpression =
		expandHeadEager() ?: head.toExpression()
	
	fun CstParseContext.expandHeadEager(): CstExpression? {
		val h = head
		if(h is CstLeafNode) {
			val kind = h.token.kind
			when(kind) {
				TokenKinds.Operator.Group.LeftParen ->
					return groupOrTupleOps()
				
				TokenKinds.Operator.Group.LeftBrace ->
					return TODO("lambda")
				
				TokenKinds.Operator.Arithmetic.Plus,
				TokenKinds.Operator.Arithmetic.Minus,
				TokenKinds.Operator.Logic.Not,
				->
					return unaryPrefixOps()
				
				else -> {}
			}
		}
		
		val peekKind = buffer.pop().token.kind
		when(peekKind) {
			TokenKinds.Operator.Group.LeftParen, TokenKinds.Operator.Group.LeftBrace ->
				return callOps()
			
			is TokenKinds.Operator.Access ->
				return accessOps()
			
			TokenKinds.Operator.Other.PropagateError ->
				return unaryPostfixOps()
			
			else -> {}
		}
		
		return null
	}
	
	fun CstParseContext.main(): Boolean {
		if(buffer.endOfInput()) {
			return if(stack.isEmpty()) {
				false
			} else {
				binaryOps()
				true
			}
		}
		
		val expression = expandHeadEager()
		if(expression != null) return true
		
		val binaryOperatorA = stack.last()
		val binaryOperatorB = buffer.peek()
		
		fun precedence(node: CstTempNode.Leaf): Int =
			(node.token.kind as TokenKinds.Operator.OperatorWithPrecedence).precedence
		
		val precedenceA = precedence(binaryOperatorA as CstTempNode.Leaf)
		val precedenceB = precedence(binaryOperatorB)
		
		if(precedenceA >= precedenceB) {
			// TODO: consider associativity
			binaryOps()
			return true
		}
		
		return true
	}
	
	fun CstParseContext.runMain(): CstExpression {
		while(true) {
			if(!main()) break
		}
		return head.toExpression()
	}
}

context(CstParseContext)
private fun CstNode.toExpression(): CstExpression = if(this is CstTempNode.TempToExpression) {
	tempToExpression()
} else {
	parseError("Unexpected node $this")
}

context(CstParseContext)
private fun CstNode.toBinaryOperator(): CstOperator.Binary = if(this is CstTempNode.Leaf) {
	tempToBinaryOperator()
} else {
	parseError("Unexpected node $this")
}

context(CstParseContext)
private fun CstNode.toUnaryOperator(): CstOperator.Unary = if(this is CstTempNode.Leaf) {
	tempToUnaryOperator()
} else {
	parseError("Unexpected node $this")
}


fun CstParseContext.cstExpression(): CstExpression = node(CstExpression) {
	with(CstExpressionParser(this)) { runMain() }
}
