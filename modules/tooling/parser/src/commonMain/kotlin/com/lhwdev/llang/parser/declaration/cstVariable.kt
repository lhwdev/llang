package com.lhwdev.llang.parser.declaration

import com.lhwdev.llang.cst.declaration.CstStandaloneVariable
import com.lhwdev.llang.cst.declaration.CstVariable
import com.lhwdev.llang.cst.declaration.CstVariableKind
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.core.cstIdentifier
import com.lhwdev.llang.parser.core.cstModifiers
import com.lhwdev.llang.parser.structuredNode
import com.lhwdev.llang.parser.type.cstDeclarationQuoteType
import com.lhwdev.llang.tokenizer.parseVariableKind


private fun CstParseContext.cstVariableKind(): CstVariableKind =
	structuredNode(CstVariableKind) { CstVariableKind(code.parseVariableKind()) }

private fun CstParseContext.cstVariableAccessor(): CstVariable.Accessor =

fun CstParseContext.cstStandaloneVariable(): CstVariable = declaration(CstStandaloneVariable) {
	CstStandaloneVariable(
		modifiers = cstModifiers(),
		kind = cstVariableKind(),
		name = cstIdentifier(),
		type = cstDeclarationQuoteType(),
		accessor = cstVariableAccessor(),
	)
}
