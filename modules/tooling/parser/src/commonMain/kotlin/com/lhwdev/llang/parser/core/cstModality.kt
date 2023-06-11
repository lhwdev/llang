package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.declaration.CstModality
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.structuredNode
import com.lhwdev.llang.parser.util.cstSeparatedList


fun CstParseContext.cstModality(): CstModality = structuredNode(CstModality) {
	CstModality(cstSeparatedList(cstModifier, separatorBlock = { cstWsOrNull() }))
}
