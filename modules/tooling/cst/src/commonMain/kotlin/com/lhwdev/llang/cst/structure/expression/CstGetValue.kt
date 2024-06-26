package com.lhwdev.llang.cst.structure.expression

import com.lhwdev.llang.cst.structure.CstNodeImpl
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstIdentifier

class CstGetValue(val value: CstIdentifier) : CstExpression, CstNodeImpl() {
	override val info
		get() = Info
	
	companion object Info : CstNodeInfo<CstGetValue> {
		override fun dummyNode() = CstGetValue(CstIdentifier.dummyNode())
	}
}
