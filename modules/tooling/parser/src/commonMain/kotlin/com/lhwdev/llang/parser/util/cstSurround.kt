package com.lhwdev.llang.parser.util

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.token.TokenKind
import com.lhwdev.llang.token.TokenKinds


class Surround(val left: TokenKind, val right: TokenKind) {
	companion object {
		val Paren = Surround(
			left = TokenKinds.Operator.Group.LeftParen,
			right = TokenKinds.Operator.Group.RightParen,
		)
	}
}


inline fun <Node : CstNode> CstParseContext.cstSurround(
	surround: Surround,
	block: CstParseContext.() -> Node,
): Node {

}
