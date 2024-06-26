package com.lhwdev.llang.cst.structure.core

import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl


class CstIdentifier(token: Token) : CstLeafNodeImpl(token) {
	override val info
		get() = Info
	
	companion object Info : CstNodeInfo<CstIdentifier> {
		override fun dummyNode() = CstIdentifier(TokenImpl.dummyIllegal())
		
	}
}
