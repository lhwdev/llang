package com.lhwdev.llang.cst.core

import com.lhwdev.llang.cst.CstNodeInfo
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl


class CstIdentifier(token: Token) : CstLeafNode(token), CstAccessTarget {
	companion object Info : CstNodeInfo<CstIdentifier> {
		override fun dummyNode() = CstIdentifier(TokenImpl.dummyIllegal())
		
	}
}
