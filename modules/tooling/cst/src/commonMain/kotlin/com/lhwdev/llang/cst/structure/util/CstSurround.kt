package com.lhwdev.llang.cst.structure.util

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeImpl
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.token.TokenKind
import com.lhwdev.llang.token.TokenKinds



class CstSurround<out Node : CstNode>(val kind: Kind, val content: Node) : CstNode, CstNodeImpl() {
	companion object Info : CstNodeInfo<CstSurround<CstNode>> {
		val Paren = Kind(
			left = TokenKinds.Operator.Group.LeftParen,
			leftContent = "(",
			right = TokenKinds.Operator.Group.RightParen,
			rightContent = ")",
		)
		
		val SquareBracket = Kind(
			left = TokenKinds.Operator.Group.LeftSquareBracket,
			leftContent = "[",
			right = TokenKinds.Operator.Group.RightSquareBracket,
			rightContent = "]",
		)
		
		@Suppress("UNCHECKED_CAST")
		fun <Node : CstNode> info() = this as CstNodeInfo<CstSurround<Node>>
		
		override fun dummyNode() = CstSurround(kind = Paren, content = CstNode.dummyNode())
	}
	
	class Kind(
		val left: TokenKind,
		val leftContent: String,
		val right: TokenKind,
		val rightContent: String,
	)
}
