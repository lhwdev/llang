package com.lhwdev.llang.parser.declaration

import com.lhwdev.llang.cst.structure.declaration.*
import com.lhwdev.llang.cst.structure.expression.CstExpression
import com.lhwdev.llang.cst.structure.util.CstOptional
import com.lhwdev.llang.parser.*
import com.lhwdev.llang.parser.core.cstIdentifier
import com.lhwdev.llang.parser.core.cstLeafNode
import com.lhwdev.llang.parser.core.cstLeafNodeOrNull
import com.lhwdev.llang.parser.core.cstModifiers
import com.lhwdev.llang.parser.expression.cstExpression
import com.lhwdev.llang.parser.statement.cstStatements
import com.lhwdev.llang.parser.type.cstDeclarationQuoteType
import com.lhwdev.llang.parser.util.cstOptional
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.parseVariableKind


private fun CstParseContext.cstVariableKind(): CstVariableKind =
	leafNode(CstVariableKind) { CstVariableKind(code.parseVariableKind()) }

private fun CstParseContext.cstVariableAccessor(): CstVariable.Accessor =
	node(CstVariable.Accessor) {
		cstDelegationAccessor() ?: CstVariable.NoAccessor
	}

private fun CstParseContext.cstDelegationAccessor(): CstVariable.Delegation? =
	nullableStructuredNode(CstVariable.Delegation) {
		cstLeafNodeOrNull(TokenKinds.SoftKeyword.By, "by") ?: return@nullableStructuredNode null
		
		CstVariable.Delegation(cstExpression())
	}

private fun CstParseContext.cstNormalAccessor(): CstVariable.Normal = cstStatements {
	val initializer = cstOptional(CstExpression) {
		cstLeafNode(TokenKinds.Operator.Assign.Assign, "=")
		cstExpression()
	}
	
	var getter: CstOptional<CstGetter> = CstOptional.None
	var setter: CstOptional<CstSetter> = CstOptional.None
	
	
	
	CstVariable.Normal(initializer, getter, setter)
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
