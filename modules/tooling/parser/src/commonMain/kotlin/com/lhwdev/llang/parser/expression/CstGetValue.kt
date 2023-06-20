package com.lhwdev.llang.parser.expression

import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstIdentifier
import com.lhwdev.llang.cst.structure.expression.CstExpression

class CstGetValue(val value: CstIdentifier) : CstExpression {
	companion object Info : CstNodeInfo<CstGetValue> {
		override fun dummyNode() = CstGetValue(CstIdentifier.dummyNode())
	}
}
