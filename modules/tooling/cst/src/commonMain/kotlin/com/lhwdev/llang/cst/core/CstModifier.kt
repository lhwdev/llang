package com.lhwdev.llang.cst.core

import com.lhwdev.llang.cst.CstLeafNode
import com.lhwdev.llang.cst.CstNodeInfo
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl
import com.lhwdev.llang.token.TokenKinds


class CstModifier(token: Token) : CstLeafNode(token) {
	companion object Info : CstNodeInfo<CstModifier> {
		override fun dummyNode() = CstModifier(TokenImpl.dummy(TokenKinds.Illegal, ""))
	}
}
