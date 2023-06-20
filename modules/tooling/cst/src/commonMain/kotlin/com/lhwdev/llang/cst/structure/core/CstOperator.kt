package com.lhwdev.llang.cst.structure.core

import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl


sealed class CstOperator(token: Token) : CstLeafNodeImpl(token) {
	class Unary(token: Token) : CstOperator(token) {
		companion object Info : CstNodeInfo<Unary> {
			override fun dummyNode() = Unary(TokenImpl.dummyIllegal())
		}
	}
	
	class Binary(token: Token) : CstOperator(token) {
		
		companion object Info : CstNodeInfo<Binary> {
			override fun dummyNode() = Binary(TokenImpl.dummyIllegal())
		}
	}
}
