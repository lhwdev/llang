package com.lhwdev.llang.cst.structure.expression

import com.lhwdev.llang.cst.structure.CstNodeInfo


class CstTuple(val elements: List<CstExpression>) : CstExpression {
	companion object Info : CstNodeInfo<CstTuple> {
		override fun dummyNode() = CstTuple(emptyList())
	}
}
