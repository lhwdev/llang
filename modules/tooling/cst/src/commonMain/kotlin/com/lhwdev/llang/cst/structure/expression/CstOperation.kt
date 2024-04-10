package com.lhwdev.llang.cst.structure.expression

import com.lhwdev.llang.cst.structure.CstNodeImpl
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstOperator

sealed class CstOperation : CstExpression, CstNodeImpl() {
	class UnaryPrefix(
		val operator: CstOperator.Unary,
		val operand: CstExpression,
	) : CstOperation() {
		override val info
			get() = Info
		
		companion object Info : CstNodeInfo<UnaryPrefix> {
			override fun dummyNode() = UnaryPrefix(
				operator = CstOperator.Unary.dummyNode(),
				operand = CstExpression.dummyNode(),
			)
		}
	}
	
	class UnaryPostfix(
		val operand: CstExpression,
		val operator: CstOperator.Unary,
	) : CstOperation() {
		override val info
			get() = Info
		
		companion object Info : CstNodeInfo<UnaryPostfix> {
			override fun dummyNode() = UnaryPostfix(
				operand = CstExpression.dummyNode(),
				operator = CstOperator.Unary.dummyNode(),
			)
		}
	}
	
	class Binary(
		val lhs: CstExpression,
		val operator: CstOperator.Binary,
		val rhs: CstExpression,
	) : CstOperation() {
		override val info
			get() = Info
		
		companion object Info : CstNodeInfo<Binary> {
			override fun dummyNode() = Binary(
				lhs = CstExpression.dummyNode(),
				operator = CstOperator.Binary.dummyNode(),
				rhs = CstExpression.dummyNode(),
			)
		}
	}
}
