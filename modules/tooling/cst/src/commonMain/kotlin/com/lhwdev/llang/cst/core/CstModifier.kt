package com.lhwdev.llang.cst.core

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.CstNodeInfo
import com.lhwdev.llang.cst.util.CstSeparatedList
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl


class CstModifier(token: Token) : CstLeafNode(token) {
	companion object Info : CstNodeInfo<CstModifier> {
		override fun dummyNode() = CstModifier(TokenImpl.dummyIllegal())
	}
}


class CstModifiers(val modifiers: CstSeparatedList<CstModifier, CstWss>) : CstNode {
	companion object Info : CstNodeInfo<CstModifiers> {
		override fun dummyNode() = CstModifiers(CstSeparatedList(emptyList()))
	}
}
