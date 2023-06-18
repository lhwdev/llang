package com.lhwdev.llang.cst.structure.expression

import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstOperator

sealed class CstOperation : CstExpression {
	class Unary(
		val operator: CstOperator.Unary,
		val operand: CstExpression,
	) : CstOperation() {
		companion object Info : CstNodeInfo<Unary> {
			override fun dummyNode() = Unary(
				operator = CstOperator.Unary.dummyNode(),
				operand = CstExpression.dummyNode(),
			)
		}
	}
	
	class Binary(
		val lhs: CstExpression,
		val operator: CstOperator.Binary,
		val rhs: CstExpression,
	) : CstOperation() {
		companion object Info : CstNodeInfo<Binary> {
			override fun dummyNode() = Binary(
				lhs = CstExpression.dummyNode(),
				operator = CstOperator.Binary.dummyNode(),
				rhs = CstExpression.dummyNode(),
			)
		}
	}
}
