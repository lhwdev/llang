package com.lhwdev.llang.cst.structure.core

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl


class CstModifier(token: Token) : CstLeafNode(token) {
	companion object Info : CstNodeInfo<CstModifier> {
		override fun dummyNode() = CstModifier(TokenImpl.dummyIllegal())
	}
}


class CstModifiers(val modifiers: List<CstModifier>) : CstNode {
	companion object Info : CstNodeInfo<CstModifiers> {
		override fun dummyNode() = CstModifiers(emptyList())
	}
}
