package com.lhwdev.llang.parser.expression

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.asInline
import com.lhwdev.llang.cst.structure.core.CstIdentifier
import com.lhwdev.llang.cst.structure.core.CstLeafNode
import com.lhwdev.llang.cst.structure.core.CstLeafNodeImpl
import com.lhwdev.llang.cst.structure.core.CstOperator
import com.lhwdev.llang.cst.structure.expression.*
import com.lhwdev.llang.cst.structure.util.CstSurround
import com.lhwdev.llang.parser.*
import com.lhwdev.llang.parsing.parseError
import com.lhwdev.llang.parsing.parseRequire
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.parseExpressionToken
import com.lhwdev.llang.tokenizer.source.eof


private class AnyLeafNode(val token: Token) : CstNode


private class NodeBuffer(context: CstParseContext) {
	private val code = context.code.cloneForRead()
	private var peekBuffer = null as AnyLeafNode?
	
	fun pop(): AnyLeafNode =
		peekBuffer.also { peekBuffer = null } ?: AnyLeafNode(code.parseExpressionToken())
	
	fun peek(): AnyLeafNode =
		peekBuffer ?: AnyLeafNode(code.parseExpressionToken()).also { peekBuffer = it }
	
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

private val DUMMY_STACK = ArrayDeque<CstNode>()

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
	var stack = DUMMY_STACK
	var head: CstNode = CstNode.dummyNode()
	
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
	
	fun CstParseContext.expression(): CstExpression = node(CstExpression) {
		markContainsDetached()
		allowUsingCodeSource()
		child { runMain() }
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
	
	fun CstParseContext.unaryPostfixOps(): CstOperation.UnaryPostfix {
		val operand = head
		val operator = buffer.pop()
		
		return restartableStructuredNode(CstOperation.UnaryPostfix) {
			markNestedContainsDetached()
			
			CstOperation.UnaryPostfix(
				operand = operand.toExpression(),
				operator = operator.toUnaryOperator(),
			)
		}.also { head = it }
	}
	
	fun CstParseContext.binaryOps(): CstOperation.Binary {
		val operator = stack.removeLast()
		val lhs = stack.removeLast()
		val rhs = head
		
		return restartableStructuredNode(CstOperation.Binary) {
			markNestedContainsDetached()
			
			CstOperation.Binary(
				lhs = lhs.toExpression(),
				operator = operator.toBinaryOperator(),
				rhs = rhs.toExpression(),
			)
		}.also { head = it }
	}
	
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
		when(buffer.peek().token.kind) {
			TokenKinds.Operator.Group.LeftParen -> {
				val arguments = cstTuple(CstSurround.Paren)
				revalidateBuffer() // cstTuple depends on original [code]; buffer remains stale without [revalidateBuffer]
				
				CstFunctionCall.Call(function, arguments)
			}
			
			TokenKinds.Operator.Group.LeftSquareBracket -> {
				val arguments = cstTuple(CstSurround.SquareBracket)
				revalidateBuffer()
				
				CstFunctionCall.Get(function, arguments)
			}
			
			TokenKinds.Operator.Group.LeftBrace -> {
				val lambda = cstLambdaExpression()
				revalidateBuffer()
				CstFunctionCall.WithLambda(function, lambda)
			}
			
			else -> error("callOps not supported with ${buffer.peek().token}")
		}
	}
	
	fun CstParseContext.groupOrTupleOps() = structuredNode(CstSurround.info()) {
		val open = leaf()
		parseRequire(open.token.kind == TokenKinds.Operator.Group.LeftParen) { "no left paren" }
		
		val content = node(info = null) {
			val content = expression()
			val next = buffer.pop()
			when(next.token.kind) {
				TokenKinds.Operator.Other.Comma -> {
					val contents = mutableListOf(content)
					while(true) {
						val otherContent = expression()
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
	
	fun CstParseContext.expandHeadEagerForExpression() = node(CstExpression) {
		markContainsDetached()
		
		while(true) {
			if(expandHeadEager() == null) break
		}
		head.toExpression()
	}
	
	fun CstParseContext.expandHeadEager(): CstExpression? {
		val h = head
		if(h is CstLeafNode) {
			when(h.token.kind) {
				TokenKinds.Operator.Group.LeftParen ->
					return groupOrTupleOps()
				
				TokenKinds.Operator.Group.LeftBrace -> {
					val lambda = cstLambdaExpression()
					revalidateBuffer()
					return lambda
				}
				
				TokenKinds.Operator.Arithmetic.Plus,
				TokenKinds.Operator.Arithmetic.Minus,
				TokenKinds.Operator.Logic.Not,
				->
					return unaryPrefixOps()
				
				else -> {}
			}
		}
		
		when(buffer.pop().token.kind) {
			TokenKinds.Operator.Group.LeftParen,
			TokenKinds.Operator.Group.LeftBrace,
			TokenKinds.Operator.Group.LeftSquareBracket,
			->
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
		
		fun precedence(node: AnyLeafNode): Int =
			(node.token.kind as TokenKinds.Operator.OperatorWithPrecedence).precedence
		
		val precedenceA = precedence(binaryOperatorA as AnyLeafNode)
		val precedenceB = precedence(binaryOperatorB)
		
		if(precedenceA >= precedenceB) {
			// TODO: consider associativity
			//       some operations can be associated left-to-right(ex: `3+4+5` -> `(3 + 4) + 5`,
			//       but some operations can't.
			//       `Associativity { LeftToRight, RightToLeft, None }`
			binaryOps()
			return true
		}
		
		push()
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
private fun CstNode.toExpression(): CstExpression = when(this) {
	is CstExpression -> this
	is AnyLeafNode -> when(token.kind) {
		is TokenKinds.Identifier -> restartableStructuredNode(CstGetValue) {
			CstGetValue(
				leafNode(CstIdentifier) { CstIdentifier(code.acceptToken(token)) },
			)
		}
		
		is TokenKinds.NumberLiteral -> restartableLeafNode(CstNumberLiteral) {
			CstNumberLiteral(code.acceptToken(token))
		}
		
		is TokenKinds.Operator -> parseError("Unexpected operator ${token.kind}")
		else -> parseError("Unexpected token $token")
	}
	
	else -> parseError("Unexpected node $this")
}

context(CstParseContext)
private fun CstNode.toBinaryOperator(): CstOperator.Binary = when(this) {
	// is CstOperator.Binary ->
	is AnyLeafNode -> when(token.kind) {
		is TokenKinds.Identifier, // infix
		is TokenKinds.Operator,
		-> restartableStructuredNode(CstOperator.Binary) {
			CstOperator.Binary(code.acceptToken(token))
		}
		
		else -> parseError("Unexpected token $token")
	}
	
	else -> parseError("Unexpected node $this")
}

context(CstParseContext)
private fun CstNode.toUnaryOperator(): CstOperator.Unary = when(this) {
	is AnyLeafNode -> when(token.kind) {
		is TokenKinds.Operator -> restartableStructuredNode(CstOperator.Unary) {
			CstOperator.Unary(code.acceptToken(token))
		}
		
		else -> parseError("Unexpected token $token")
	}
	
	else -> parseError("Unexpected node $this")
}


fun CstParseContext.cstExpression(): CstExpression =
	with(CstExpressionParser(this)) {
		val expr = expression()
		buffer.close()
		expr
	}
