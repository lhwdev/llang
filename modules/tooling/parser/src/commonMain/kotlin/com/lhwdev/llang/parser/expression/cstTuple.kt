package com.lhwdev.llang.parser.expression

import com.lhwdev.llang.cst.structure.expression.CstTuple
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.core.cstLeafCommaOrNull
import com.lhwdev.llang.parser.structuredNode
import com.lhwdev.llang.parser.util.cstSeparatedList


fun CstParseContext.cstTuple(): CstTuple = structuredNode(CstTuple) {
	val elements = cstSeparatedList(
		itemBlock = { cstExpression() },
		separatorBlock = { cstLeafCommaOrNull() }
	)
	CstTuple(elements)
}
