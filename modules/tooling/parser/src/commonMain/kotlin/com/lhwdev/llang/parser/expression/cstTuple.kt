package com.lhwdev.llang.parser.expression

import com.lhwdev.llang.cst.structure.expression.CstTuple
import com.lhwdev.llang.cst.structure.util.CstSurround
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.core.cstLeafCommaOrNull
import com.lhwdev.llang.parser.structuredNode
import com.lhwdev.llang.parser.util.cstSeparatedList
import com.lhwdev.llang.parser.util.cstSurround


fun CstParseContext.cstTuple(
	surround: CstSurround.Kind = CstSurround.Paren,
): CstTuple = structuredNode(CstTuple) {
	val node = cstSurround(surround) {
		cstSeparatedList(
			itemBlock = { cstExpression() },
			separatorBlock = { cstLeafCommaOrNull() },
		)
	}
	CstTuple(node.content.items())
}
