package com.lhwdev.llang.cst.declaration

import com.lhwdev.llang.cst.CstNodeInfo
import com.lhwdev.llang.cst.core.CstLeafNode
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl
import com.lhwdev.llang.token.TokenKinds


class CstVisibility(token: Token) : CstLeafNode(token) {
	companion object Info : CstNodeInfo<CstVisibility> {
		override fun dummyNode() =
			CstVisibility(TokenImpl.dummy(TokenKinds.Modifier.Internal, "internal"))
	}
}
