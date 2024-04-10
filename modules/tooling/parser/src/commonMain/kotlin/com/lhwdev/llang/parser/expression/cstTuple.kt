package com.lhwdev.llang.parser.expression

import com.lhwdev.llang.cst.structure.expression.CstTuple
import com.lhwdev.llang.cst.structure.util.CstSurround
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.structuredNode
import com.lhwdev.llang.parser.util.cstCommaSeparatedList
import com.lhwdev.llang.parser.util.cstSurround


fun CstParseContext.cstTuple(
	surround: CstSurround.Kind = CstSurround.Paren,
): CstTuple = structuredNode {
	val node = cstSurround(surround) {
		cstCommaSeparatedList { cstExpression() }
	}
	CstTuple(node.content.items())
}
