package com.lhwdev.llang.cst.structure.declaration

import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstLeafNode
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl
import com.lhwdev.llang.token.TokenKinds


class CstVisibility(token: Token) : CstLeafNode(token) {
	companion object Info : CstNodeInfo<CstVisibility> {
		override fun dummyNode() =
			CstVisibility(TokenImpl.dummy(TokenKinds.Modifier.Internal, "internal"))
	}
}
