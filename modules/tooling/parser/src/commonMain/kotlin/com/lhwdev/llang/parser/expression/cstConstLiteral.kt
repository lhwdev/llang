package com.lhwdev.llang.parser.expression

import com.lhwdev.llang.cst.structure.expression.CstConstLiteral
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.leafNode


fun CstParseContext.cstConstLiteral(): CstConstLiteral = leafNode(null) {
	code.parse
}


fun CstParseContext.cstStringLiteral(): CstConstLiteral.String
