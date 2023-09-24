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
import com.lhwdev.llang.parsing.debug
import com.lhwdev.llang.parsing.parseError
import com.lhwdev.llang.parsing.parseRequire
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKind
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.parseExpressionToken
import com.lhwdev.llang.tokenizer.source.current
import com.lhwdev.llang.tokenizer.source.eof


private class CstExpressionLeafNode(token: Token) : CstLeafNodeImpl(token) {
	override fun toString() = token.toString()
}


private class NodeBuffer {
	private var peekBuffer = null as CstNode?
	
	context(CstParseContext)
	private fun nextNode(): CstNode {
		val node = nullableLeafNode {
			code.parseExpressionToken()?.let { CstExpressionLeafNode(it) }
		}
		
		return node ?: when(code.current) {
			'"' -> cstStringLiteral()
			else -> error("?!")
		}
	}
	
	context(CstParseContext)
	fun pop(): CstNode {
		peekBuffer = null
		return nextNode()
	}
	
	context(CstParseContext)
	fun popLeaf(): CstExpressionLeafNode = pop() as CstExpressionLeafNode
	
	context(CstParseContext)
	fun peek(): CstNode = peekBuffer ?: run {
		val next = rawNode(CstParseContext.NodeKind.Peek) { nextNode() }
		peekBuffer = next
		next
	}
	
	context(CstParseContext)
	fun peekKind(): TokenKind? = (peek() as? CstExpressionLeafNode)?.token?.kind
	
	context(CstParseContext)
	fun endOfInput(): Boolean {
		if(code.eof) return true
		
		val next = peek()
		if(next !is CstExpressionLeafNode) return false
		val kind = next.token.kind
		
		return when {
			kind is TokenKinds.Operator.Group && !kind.open -> true
			kind == TokenKinds.Operator.Other.Comma -> true
			else -> false
		}
	}
	
	context(CstParseContext)
	fun close() {
		code.close()
	}
}

/**
 * Considerations:
 * - end of statement; LineBreak-separated statements
 * - precedence(is already considered)
 * - determining 'is this expression vs binaryOps vs unaryOps' is easier than you thought
 */
private class CstExpressionParser {
	private val debugSelf = false
	
	var head: CstExpression = CstExpression.dummyNode()
	val buffer = NodeBuffer()
	val expressions = ArrayDeque<CstExpression>()
	val binaryOperators = ArrayDeque<CstOperator.Binary>()
	
	inline fun <Node : CstNode> externalNode(block: () -> Node): Node {
		// buffer.close()
		val node = block()
		// buffer = NodeBuffer()
		return node
	}
	
	fun CstParseContext.debugPhase(phase: String) {
		if(debugSelf) debug {
			"expr/${phase}: head=${head} " +
				"expressions=${expressions.take(3)} binaryOps=${binaryOperators.take(3)}"
		}
	}
	
	fun CstParseContext.cstExpressionInner(): CstExpression {
		markChildrenAsDetached()
		provideNodeHintToCurrent(CstParseContext.NodeHint.AllowCodeSourceAccess)
		head = buffer.pop().toExpressionDetached()
		return runMain()
	}
	
	private fun CstParseContext.leaf(node: CstExpressionLeafNode = buffer.popLeaf()): CstLeafNode =
		leafNode(CstLeafNode) { insertChildNode(node) }
	
	fun CstParseContext.unaryPrefixOps() = structuredNode(CstOperation.UnaryPrefix) {
		debugPhase("unaryPrefixOps")
		val operator = head.toUnaryOperator()
		val operand = expandExpression()
		
		CstOperation.UnaryPrefix(
			operator = operator,
			operand = insertChildNode(operand),
		)
	}
	
	fun CstParseContext.unaryPostfixOps(): CstOperation.UnaryPostfix {
		debugPhase("unaryPostfixOps")
		val operand = head
		val operator = buffer.pop()
		
		return restartableStructuredNode(CstOperation.UnaryPostfix) {
			CstOperation.UnaryPostfix(
				operand = insertChildNode(operand),
				operator = operator.toUnaryOperator(),
			)
		}.also { head = it }
	}
	
	fun CstParseContext.binaryOps(): CstOperation.Binary {
		debugPhase("binaryOps")
		val operator = binaryOperators.removeAt(binaryOperators.size - 2)
		val rhs = expressions.removeLast()
		val lhs = expressions.removeLast()
		
		return restartableStructuredNode(CstOperation.Binary) {
			CstOperation.Binary(
				lhs = insertChildNode(lhs),
				operator = operator.toBinaryOperator(),
				rhs = insertChildNode(rhs),
			)
		}.also { head = it }
	}
	
	fun CstParseContext.binaryOpsOnEoi(): CstOperation.Binary {
		debugPhase("binaryOpsOnEoi")
		val operator = binaryOperators.removeLast()
		val lhs = expressions.removeLast()
		val rhs = head
		
		return restartableStructuredNode(CstOperation.Binary) {
			CstOperation.Binary(
				lhs = insertChildNode(lhs),
				operator = operator.toBinaryOperator(),
				rhs = insertChildNode(rhs),
			)
		}.also { head = it }
		
	}
	
	fun CstParseContext.accessOps() = node(info = null) {
		debugPhase("accessOps")
		val parent = head
		val accessor = leafNode(CstLeafNode) {
			val node = buffer.popLeaf()
			insertChildNode(node)
			node
		}
		val item = popAndExpandExpression()
		parseRequire(accessor.token.kind == TokenKinds.Operator.Access.Dot) {
			"TODO: implement accessor for :: Metadata and ?. SafeDot"
		}
		
		structuredNode(CstMemberAccess.asInline()) {
			CstMemberAccess(
				parent = insertChildNode(parent),
				item = insertChildNode(item),
			)
		}
	}.also { head = it }
	
	fun CstParseContext.callOps() = node(info = null) {
		debugPhase("callOps")
		val function = insertChildNode(head)
		when(buffer.peekKind()) {
			TokenKinds.Operator.Group.LeftParen -> {
				val arguments = externalNode { cstTuple(CstSurround.Paren) }
				CstFunctionCall.Call(function, arguments)
			}
			
			TokenKinds.Operator.Group.LeftSquareBracket -> {
				val arguments = externalNode { cstTuple(CstSurround.SquareBracket) }
				CstFunctionCall.Get(function, arguments)
			}
			
			TokenKinds.Operator.Group.LeftBrace -> {
				val lambda = externalNode { cstLambdaExpression() }
				CstFunctionCall.WithLambda(function, lambda)
			}
			
			else -> error("callOps not supported with ${buffer.peek()}")
		}
	}
	
	fun CstParseContext.groupOrTupleOps() = structuredNode(CstSurround.info()) {
		debugPhase("groupOrTupleOps")
		val open = leaf()
		parseRequire(open.token.kind == TokenKinds.Operator.Group.LeftParen) { "no left paren" }
		
		val content = node(info = null) {
			val content = externalNode { cstExpression() }
			val next = buffer.popLeaf()
			when(next.token.kind) {
				TokenKinds.Operator.Other.Comma -> {
					val contents = mutableListOf(content)
					while(true) {
						val otherContent = externalNode { cstExpression() }
						contents += otherContent
						
						val comma = leaf()
						when(comma.token.kind) {
							TokenKinds.Operator.Other.Comma -> {
								// trailing comma available
								if(buffer.peekKind() == TokenKinds.Operator.Group.RightParen) {
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
					leaf(next)
					content
				}
				
				else -> parseError("Unknown end of child expression parsing")
			}
		}
		CstSurround(kind = CstSurround.Paren, content)
	}.content
	
	fun CstParseContext.popAndExpandExpression(): CstExpression {
		head = buffer.pop().toExpressionDetached()
		return expandExpression()
	}
	
	fun CstParseContext.expandExpression() = node(CstExpression) {
		markChildrenAsDetached()
		
		while(true) {
			if(expandExpressionInner() == null) break
		}
		insertChildNode(head)
	}
	
	fun CstParseContext.expandExpressionInner(): CstExpression? {
		debugPhase("expandExpressionInner")
		val h = head
		if(h is CstLeafNode) {
			when(h.token.kind) {
				TokenKinds.Operator.Group.LeftParen ->
					return groupOrTupleOps()
				
				TokenKinds.Operator.Group.LeftBrace -> {
					return externalNode { cstLambdaExpression() }
				}
				
				TokenKinds.Operator.Arithmetic.Plus,
				TokenKinds.Operator.Arithmetic.Minus,
				TokenKinds.Operator.Logic.Not,
				->
					return unaryPrefixOps()
				
				else -> {}
			}
		}
		
		when(buffer.peekKind()) {
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
		debugPhase("main")
		if(buffer.endOfInput()) {
			return if(expressions.isEmpty()) {
				false
			} else {
				binaryOpsOnEoi()
				true
			}
		}
		
		val expression = expandExpressionInner()
		if(expression != null) return true
		
		pushForBinaryOps()
		
		val binaryOperatorB = binaryOperators.getOrNull(binaryOperators.size - 1)
			?: parseError("???")
		val binaryOperatorA = binaryOperators.getOrNull(binaryOperators.size - 2)
			?: return true
		
		if(binaryOperatorA.precedence >= binaryOperatorB.precedence) {
			// TODO: consider associativity
			//   some operations can be associated left-to-right(ex: `3+4+5` -> `(3 + 4) + 5`,
			//   but some operations can't.
			//   Like `Associativity { LeftToRight, RightToLeft, None }`
			binaryOps()
			return true
		}
		return true
	}
	
	private fun CstParseContext.pushForBinaryOps() {
		debugPhase("pushForBinaryOps")
		expressions.addLast(head)
		
		val operator = detached { buffer.popLeaf().toBinaryOperator() }
		binaryOperators.addLast(operator)
		
		head = buffer.popLeaf().toExpressionDetached()
	}
	
	fun CstParseContext.runMain(): CstExpression {
		while(true) {
			if(!main()) break
		}
		return head
	}
}


/// Note: All 'toXXX' helpers assume that you provide detached node, and it returns attached node.

context(CstParseContext)
private fun CstNode.toExpressionDetached(): CstExpression = when(this) {
	is CstExpression -> this
	is CstExpressionLeafNode -> when(token.kind) {
		is TokenKinds.Identifier -> restartableStructuredNode(CstGetValue) {
			markCurrentAsDetached()
			CstGetValue(
				leafNode(CstIdentifier) {
					CstIdentifier(insertChildNode(this@toExpressionDetached).token)
				},
			)
		}
		
		is TokenKinds.NumberLiteral -> restartableLeafNode(CstConstLiteral.Number) {
			markCurrentAsDetached()
			CstConstLiteral.Number(insertChildNode(this@toExpressionDetached).token)
		}
		
		is TokenKinds.Operator -> parseError("Unexpected operator ${token.kind}")
		else -> parseError("Unexpected token $token")
	}
	
	else -> parseError("Unexpected node $this")
}

context(CstParseContext)
private fun CstNode.toBinaryOperator(): CstOperator.Binary = when(this) {
	is CstOperator.Binary -> insertChildNode(this)
	is CstExpressionLeafNode -> when(token.kind) {
		is TokenKinds.Identifier, // infix
		is TokenKinds.Operator,
		-> restartableStructuredNode(CstOperator.Binary) {
			CstOperator.Binary(insertChildNode(this@toBinaryOperator).token)
		}
		
		else -> parseError("Unexpected token $token")
	}
	
	else -> parseError("Unexpected node $this")
}

context(CstParseContext)
private fun CstNode.toUnaryOperator(): CstOperator.Unary = when(this) {
	is CstOperator.Unary -> insertChildNode(this)
	is CstExpressionLeafNode -> when(token.kind) {
		is TokenKinds.Operator -> restartableStructuredNode(CstOperator.Unary) {
			CstOperator.Unary(insertChildNode(this@toUnaryOperator).token)
		}
		
		else -> parseError("Unexpected token $token")
	}
	
	else -> parseError("Unexpected node $this")
}


fun CstParseContext.cstExpression(): CstExpression = structuredNode {
	with(CstExpressionParser()) {
		val expr = cstExpressionInner()
		buffer.close()
		expr
	}
}
