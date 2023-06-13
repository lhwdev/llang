package com.lhwdev.llang.cst.core

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.CstNodeInfo
import com.lhwdev.llang.cst.util.CstList
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl


class CstModifier(token: Token) : CstLeafNode(token) {
	companion object Info : CstNodeInfo<CstModifier> {
		override fun dummyNode() = CstModifier(TokenImpl.dummyIllegal())
	}
}


class CstModifiers(val modifiers: CstList<CstModifier>) : CstNode {
	companion object Info : CstNodeInfo<CstModifiers> {
		override fun dummyNode() = CstModifiers(CstList(emptyList()))
	}
}
