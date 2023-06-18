package com.lhwdev.llang.cst.structure.core

import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.type.CstTypeAccessTarget
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl


class CstIdentifier(token: Token) : CstLeafNodeImpl(token), CstTypeAccessTarget {
	companion object Info : CstNodeInfo<CstIdentifier> {
		override fun dummyNode() = CstIdentifier(TokenImpl.dummyIllegal())
		
	}
}
