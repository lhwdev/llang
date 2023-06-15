package com.lhwdev.llang.parser.declaration

import com.lhwdev.llang.cst.declaration.CstStandaloneVariable
import com.lhwdev.llang.cst.declaration.CstVariable
import com.lhwdev.llang.cst.declaration.CstVariableKind
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.core.cstIdentifier
import com.lhwdev.llang.parser.core.cstLeafNodeOrNull
import com.lhwdev.llang.parser.core.cstModifiers
import com.lhwdev.llang.parser.node
import com.lhwdev.llang.parser.structuredNode
import com.lhwdev.llang.parser.type.cstDeclarationQuoteType
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.parseVariableKind


private fun CstParseContext.cstVariableKind(): CstVariableKind =
	structuredNode(CstVariableKind) { CstVariableKind(code.parseVariableKind()) }

private fun CstParseContext.cstVariableAccessor(): CstVariable.Accessor =
	node(CstVariable.Accessor) {
		cstDelegationAccessor() ?: CstVariable.NoAccessor
	}

private fun CstParseContext.cstDelegationAccessor(): CstVariable.Delegation? {
	cstLeafNodeOrNull(TokenKinds.SoftKeyword.By, "by") ?: return null
	
	CstVariable.Delegation(cstExpression())
}


fun CstParseContext.cstStandaloneVariable(): CstVariable = declaration(CstStandaloneVariable) {
	CstStandaloneVariable(
		annotations = cstAnnotations(),
		context = cstContextDeclaration(),
		modifiers = cstModifiers(), // public open abstract context(...)
		kind = cstVariableKind(),
		name = cstIdentifier(),
		type = cstDeclarationQuoteType(),
		accessor = cstVariableAccessor(),
	)
}
