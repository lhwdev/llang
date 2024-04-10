package com.lhwdev.llang.cst.structure.core

import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl
import com.lhwdev.llang.token.TokenKinds


sealed class CstOperator(token: Token) : CstLeafNodeImpl(token) {
	val precedence: Int
		get() = (token.kind as? TokenKinds.Operator.OperatorWithPrecedence)?.precedence ?: -1
	
	class Unary(token: Token) : CstOperator(token) {
		override val info
			get() = Info
		
		override fun toString() = "CstOperator.Unary(${token.code})"
		
		companion object Info : CstNodeInfo<Unary> {
			override fun dummyNode() = Unary(TokenImpl.dummyIllegal())
		}
	}
	
	class Binary(token: Token) : CstOperator(token) {
		override val info
			get() = Info
		
		override fun toString() = "CstOperator.Binary(${token.code})"
		
		companion object Info : CstNodeInfo<Binary> {
			override fun dummyNode() = Binary(TokenImpl.dummyIllegal())
		}
	}
}
