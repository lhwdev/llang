package com.lhwdev.llang.cst.structure.expression

import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstIdentifier

class CstGetValue(val value: CstIdentifier) : CstExpression {
	companion object Info : CstNodeInfo<CstGetValue> {
		override fun dummyNode() = CstGetValue(CstIdentifier.dummyNode())
	}
}
