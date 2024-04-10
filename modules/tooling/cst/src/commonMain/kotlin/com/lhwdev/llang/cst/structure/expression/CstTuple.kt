package com.lhwdev.llang.cst.structure.expression

import com.lhwdev.llang.cst.structure.CstNodeImpl
import com.lhwdev.llang.cst.structure.CstNodeInfo


class CstTuple(val elements: List<CstExpression>) : CstExpression, CstNodeImpl() {
	override val info
		get() = Info
	
	companion object Info : CstNodeInfo<CstTuple> {
		override fun dummyNode() = CstTuple(emptyList())
	}
}
