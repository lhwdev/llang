package com.lhwdev.llang.parser.expression

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.core.CstLeafNode
import com.lhwdev.llang.cst.structure.core.CstOperator
import com.lhwdev.llang.cst.structure.expression.CstAccessExpression
import com.lhwdev.llang.cst.structure.expression.CstExpression
import com.lhwdev.llang.cst.structure.expression.CstOperation
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.node
import com.lhwdev.llang.parser.structuredNode
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
private class CstExpressionParser(private val context: CstParseContext) {
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
		
		return structuredNode(CstOperation.UnaryPrefix) {
			CstOperation.UnaryPrefix(
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
		parseRequire(accessor.token.kind == TokenKinds.Operation.Access.Dot) {
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
				TokenKinds.Operation.Group.LeftParen ->
					return groupOrTupleOps()
				
				TokenKinds.Operation.Group.LeftBrace ->
					return TODO("lambda")
				
				TokenKinds.Operation.Arithmetic.Plus, TokenKinds.Operation.Arithmetic.Minus ->
					return unaryPrefixOps()
			}
		}
		
		val peekKind = buffer.pop().token.kind
		return when(peekKind) {
			TokenKinds.Operation.Group.LeftParen, TokenKinds.Operation.Group.LeftBrace ->
				callOps()
			
			is TokenKinds.Operation.Access ->
				accessOps()
			
			else -> null
		}
	}
}


private fun CodeSource.cstLeafExpression(): CstUnclassifiedLeafNode =
	CstUnclassifiedLeafNode(parseExpressionToken())

private fun CstNode.toExpression(): CstExpression = TODO()

private fun CstNode.toBinaryOperator(): CstOperator.Binary = TODO()

private fun CstNode.toUnaryOperator(): CstOperator.Unary = TODO()


fun CstParseContext.cstExpression(): CstExpression = node(CstExpression) {
	
	
	TODO("the hardest thing")
}
