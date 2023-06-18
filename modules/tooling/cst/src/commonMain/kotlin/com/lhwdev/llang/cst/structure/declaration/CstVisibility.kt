package com.lhwdev.llang.cst.structure.declaration

import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstLeafNodeImpl
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl
import com.lhwdev.llang.token.TokenKinds


class CstVisibility(token: Token) : CstLeafNodeImpl(token) {
	companion object Info : CstNodeInfo<CstVisibility> {
		override fun dummyNode() =
			CstVisibility(TokenImpl.dummy(TokenKinds.Modifier.Internal, "internal"))
	}
}
