package com.lhwdev.llang.parser.declaration

import com.lhwdev.llang.cst.declaration.CstVariable
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.structuredNode


fun CstParseContext.cstVariable(): CstVariable = structuredNode(CstVariable) {
	CstVariable(
	
	)
}
