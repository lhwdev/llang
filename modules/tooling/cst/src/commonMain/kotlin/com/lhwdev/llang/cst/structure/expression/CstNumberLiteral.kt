package com.lhwdev.llang.cst.structure.expression

import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstLeafNode
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl

class CstNumberLiteral(override val token: Token) : CstExpression, CstLeafNode {
	companion object Info : CstNodeInfo<CstNumberLiteral> {
		override fun dummyNode() = CstNumberLiteral(TokenImpl.dummyIllegal())
	}
}
