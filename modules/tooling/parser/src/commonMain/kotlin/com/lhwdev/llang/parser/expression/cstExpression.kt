package com.lhwdev.llang.parser.expression

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.core.CstIdentifier
import com.lhwdev.llang.cst.structure.core.CstLeafNode
import com.lhwdev.llang.cst.structure.core.CstOperator
import com.lhwdev.llang.cst.structure.expression.CstAccessExpression
import com.lhwdev.llang.cst.structure.expression.CstExpression
import com.lhwdev.llang.cst.structure.expression.CstOperation
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.leafNode
import com.lhwdev.llang.parser.node
import com.lhwdev.llang.parser.structuredNode
import com.lhwdev.llang.parsing.util.parseError
import com.lhwdev.llang.parsing.util.parseRequire
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.parseExpressionToken
import com.lhwdev.llang.tokenizer.source.CodeSource


/**
 * Special node that is not accepted yet into main [CstParseContext.code].
 * [token] should be accepted to be sound.
 *
 * @see com.lhwdev.llang.parser.CstCodeSource.acceptToken
 */
private class CstUnclassifiedLeafNode(val token: Token) : CstNode

private class NodeBuffer(private val context: CstParseContext) {
	private val code = context.code.cloneForRead()
	private var peekBuffer = null as CstUnclassifiedLeafNode?
	
	fun pop(): CstUnclassifiedLeafNode =
		peekBuffer.also { peekBuffer = null } ?: code.cstLeafExpression()
	
	fun peek(): CstUnclassifiedLeafNode =
		peekBuffer ?: code.cstLeafExpression().also { peekBuffer = it }
	
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
private class CstExpressionParser(context: CstParseContext) {
	val buffer = NodeBuffer(context)
	val stack = ArrayDeque<CstNode>()
	var head: CstNode = buffer.pop()
	
	fun push() {
		stack.addLast(head)
		stack.addLast(buffer.pop())
		head = buffer.pop()
	}
	
	fun CstParseContext.unaryPrefixOps(): CstExpression {
		val operator = head
		val operand = expandHeadEagerForExpression(buffer.pop())
		
		return structuredNode(CstOperation.UnaryPrefix) {
			CstOperation.UnaryPrefix(
				operator = operator.toUnaryOperator(),
				operand = operand,
			)
		}.also { head = it }
	}
	
	fun CstParseContext.unaryPostfixOps(): CstExpression {
		val operand = expandHeadEagerForExpression(head)
		val operator = buffer.pop()
		
		return structuredNode(CstOperation.UnaryPostfix) {
			CstOperation.UnaryPostfix(
				operand = operand,
				operator = operator.toUnaryOperator(),
			)
		}.also { head = it }
	}
	
	fun CstParseContext.binaryOps(): CstExpression {
		val operator = stack.removeLast()
		val lhs = stack.removeLast()
		val rhs = head
		return structuredNode(CstOperation.Binary) {
			CstOperation.Binary(
				lhs = lhs.toExpression(),
				operator = operator.toBinaryOperator(),
				rhs = rhs.toExpression(),
			)
		}.also { head = it }
	}
	
	fun CstParseContext.accessOps(): CstExpression {
		val parent = head
		val accessor = buffer.pop()
		val item = expandHeadEagerForExpression(buffer.pop())
		parseRequire(accessor.token.kind == TokenKinds.Operator.Access.Dot) {
			"TODO: implement accessor for :: Metadata and ?. SafeDot"
		}
		return structuredNode(CstAccessExpression) {
			CstAccessExpression(
				parent = head.toExpression(),
				item = item,
			)
		}.also { head = it }
	}
	
	fun CstParseContext.expandHeadEagerForExpression(h: CstNode): CstExpression =
		expandHeadEager(h) ?: head.toExpression()
	
	fun CstParseContext.expandHeadEager(h: CstNode): CstExpression? {
		head = h
		
		if(h is CstLeafNode) {
			val kind = h.token.kind
			when(kind) {
				TokenKinds.Operator.Group.LeftParen ->
					return groupOrTupleOps()
				
				TokenKinds.Operator.Group.LeftBrace ->
					return TODO("lambda")
				
				TokenKinds.Operator.Arithmetic.Plus, TokenKinds.Operator.Arithmetic.Minus ->
					return unaryPrefixOps()
			}
		}
		
		val peekKind = buffer.pop().token.kind
		return when(peekKind) {
			TokenKinds.Operator.Group.LeftParen, TokenKinds.Operator.Group.LeftBrace ->
				callOps()
			
			is TokenKinds.Operator.Access ->
				accessOps()
			
			else -> null
		}
	}
	
	fun CstParseContext.main(): Boolean {
		return true
	}
}


private fun CodeSource.cstLeafExpression(): CstUnclassifiedLeafNode =
	CstUnclassifiedLeafNode(parseExpressionToken())

context(CstParseContext)
private fun CstNode.toExpression(): CstExpression = when(this) {
	is CstExpression -> this
	is CstUnclassifiedLeafNode -> when(token.kind) {
		is TokenKinds.Identifier -> structuredNode(CstGetValue) {
			CstGetValue(
				leafNode(CstIdentifier) { CstIdentifier(code.acceptToken(token)) }
			)
		}
		
		is TokenKinds.NumberLiteral -> leafNode(CstNumberLiteral) {
			CstNumberLiteral(code.acceptToken(token))
		}
		
		is TokenKinds.Operator -> parseError("Unexpected operator ${token.kind}")
		else -> parseError("Unexpected token $token")
	}
	
	else -> parseError("Unexpected node $this")
}

context(CstParseContext)
private fun CstNode.toBinaryOperator(): CstOperator.Binary = when(this) {
	is CstUnclassifiedLeafNode -> when(token.kind) {
		is TokenKinds.Operator -> leafNode(CstOperator.Binary) {
			CstOperator.Binary(code.acceptToken(token))
		}
		
		else -> parseError("Unexpected token $token")
	}
	
	else -> parseError("Unexpected node $this")
}

context(CstParseContext)
private fun CstNode.toUnaryOperator(): CstOperator.Unary = when(this) {
	is CstUnclassifiedLeafNode -> when(token.kind) {
		is TokenKinds.Operator -> leafNode(CstOperator.Unary) {
			CstOperator.Unary(code.acceptToken(token))
		}
		
		else -> parseError("Unexpected token $token")
	}
	
	else -> parseError("Unexpected node $this")
}


fun CstParseContext.cstExpression(): CstExpression = node(CstExpression) {
	val parser = CstExpressionParser(this)
	
	with(parser) {
		while(true) {
			if(!main()) break
		}
	}
	parser.head.toExpression()
}
