package com.lhwdev.llang.parser.expression

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstLeafNode
import com.lhwdev.llang.cst.structure.core.CstLeafNodeImpl
import com.lhwdev.llang.cst.structure.core.CstOperator
import com.lhwdev.llang.cst.structure.expression.CstAccessExpression
import com.lhwdev.llang.cst.structure.expression.CstExpression
import com.lhwdev.llang.cst.structure.expression.CstOperation
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.leafNode
import com.lhwdev.llang.parser.node
import com.lhwdev.llang.parser.structuredNode
import com.lhwdev.llang.parsing.util.parseRequire
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.parseExpressionToken


class CstUnclassifiedLeafNode(token: Token) : CstLeafNodeImpl(token) {
	companion object Info : CstNodeInfo<CstUnclassifiedLeafNode> {
		override fun dummyNode() = CstUnclassifiedLeafNode(TokenImpl.dummyIllegal())
	}
}

private class NodeBuffer(private val context: CstParseContext) {
	private var peekBuffer = null as CstUnclassifiedLeafNode?
	
	fun pop(): CstUnclassifiedLeafNode =
		peekBuffer.also { peekBuffer = null } ?: context.cstLeafExpression()
	
	fun peek(): CstUnclassifiedLeafNode =
		peekBuffer ?: context.cstLeafExpression().also { peekBuffer = it }
	
	fun end() {
		require(peekBuffer == null) { "peekBuffer is not null" }
	}
}

private class CstExpressionParser(context: CstParseContext) {
	val buffer = NodeBuffer(context)
	val stack = ArrayDeque<CstNode>()
	var head: CstNode = buffer.pop() // initialPush
	
	fun nextEagerExpression(): CstExpression {
		TODO()
	}
	
	fun push() {
		stack.addLast(head)
		stack.addLast(buffer.pop())
		head = buffer.pop()
	}
	
	fun CstParseContext.unaryOps() {
		val operator = head
		val operand = expandHeadEagerForExpression(buffer.pop())
		
		head = structuredNode(CstOperation.Unary) {
			CstOperation.Unary(
				operator = operator.asUnaryOperator(),
				operand = operand,
			)
		}
	}
	
	fun CstParseContext.binaryOps() {
		val operator = stack.removeLast()
		val lhs = stack.removeLast()
		val rhs = head
		head = structuredNode(CstOperation.Binary) {
			CstOperation.Binary(
				lhs = lhs.asExpression(),
				operator = operator.asBinaryOperator(),
				rhs = rhs.asExpression(),
			)
		}
	}
	
	fun CstParseContext.accessOps() {
		val parent = head
		val accessor = buffer.pop()
		val item = expandHeadEagerForExpression(buffer.pop())
		// TODO: more kind of accessor . :: ?.
		parseRequire(accessor.token.kind == TokenKinds.Operation.Access.Dot) { "accessor not Dot(`.`)" }
		head = structuredNode(CstAccessExpression) {
			CstAccessExpression(
				parent = head.asExpression(),
				item = item,
			)
		}
	}
	
	fun CstParseContext.expandHeadEagerForExpression(h: CstLeafNode): CstExpression =
		expandHeadEager(h) ?: head.asExpression()
	
	fun CstParseContext.expandHeadEager(h: CstLeafNode): CstExpression? {
		head = h
		val kind = h.token.kind
		return when {
			kind == TokenKinds.Operation.Group.LeftParen -> groupOrTupleOps()
			kind == TokenKinds.Operation.Group.LeftBrace -> TODO("lambda")
			kind is TokenKinds.Operation.
		}
	}
}


private fun CstParseContext.cstLeafExpression(): CstUnclassifiedLeafNode =
	leafNode(CstUnclassifiedLeafNode) {
		CstUnclassifiedLeafNode(code.parseExpressionToken())
	}

private fun CstNode.asExpression(): CstExpression = TODO()

private fun CstNode.asBinaryOperator(): CstOperator.Binary = TODO()

private fun CstNode.asUnaryOperator(): CstOperator.Unary = TODO()


fun CstParseContext.cstExpression(): CstExpression = node(CstExpression) {
	
	
	TODO("the hardest thing")
}
