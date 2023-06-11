package com.lhwdev.llang.cst

import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl
import com.lhwdev.llang.token.TokenKinds


/**
 * Node for tokens with unimportant token kind, such as `Comma`, `Dot` etc.
 */
open class CstLeafNode(val token: Token) : CstNode {
	companion object Info : CstNodeInfo<CstLeafNode> {
		override fun dummyNode() = CstLeafNode(TokenImpl.dummy(TokenKinds.Illegal, ""))
	}
}
