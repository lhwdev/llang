package com.lhwdev.llang.parser.expression

import com.lhwdev.llang.cst.structure.expression.CstExpression
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.node


fun CstParseContext.cstExpression(): CstExpression = node(CstExpression) {
	// this is the BIG SHOT !!
	// precedence parsing, context parsing ...
	// TODO: do this later
	TODO("the hardest thing")
}
