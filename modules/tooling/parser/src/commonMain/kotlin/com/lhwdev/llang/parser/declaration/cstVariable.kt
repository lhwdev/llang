package com.lhwdev.llang.parser.declaration

import com.lhwdev.llang.cst.structure.declaration.CstDeclarations
import com.lhwdev.llang.cst.structure.declaration.CstStandaloneVariable
import com.lhwdev.llang.cst.structure.declaration.CstVariable
import com.lhwdev.llang.cst.structure.declaration.CstVariableKind
import com.lhwdev.llang.cst.structure.expression.CstExpression
import com.lhwdev.llang.parser.*
import com.lhwdev.llang.parser.core.cstIdentifier
import com.lhwdev.llang.parser.core.cstLeafNode
import com.lhwdev.llang.parser.core.cstModifiers
import com.lhwdev.llang.parser.core.cstSoftKeywordOrNull
import com.lhwdev.llang.parser.expression.cstExpression
import com.lhwdev.llang.parser.statement.cstStatements
import com.lhwdev.llang.parser.type.cstDeclarationQuoteTypeOrNone
import com.lhwdev.llang.parser.util.cstOptional
import com.lhwdev.llang.parser.util.items
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.parseVariableKind


private fun CstParseContext.cstVariableKind(): CstVariableKind = leafNode(CstVariableKind) {
	CstVariableKind(code.parseVariableKind())
}.also {
	preventDiscard()
}

private fun CstParseContext.cstVariableAccessor(): CstVariable.Accessor =
	node(CstVariable.Accessor) {
		cstDelegationAccessor() ?: discardable { cstNormalAccessor() } ?: CstVariable.NoAccessor
	}

private fun CstParseContext.cstDelegationAccessor(): CstVariable.Delegation? =
	nullableStructuredNode(CstVariable.Delegation) {
		cstSoftKeywordOrNull(TokenKinds.SoftKeyword.By, "by") ?: return@nullableStructuredNode null
		
		CstVariable.Delegation(cstExpression())
	}


private fun CstParseContext.cstNormalAccessor(): CstVariable.Normal = cstStatements {
	val initializer = item {
		cstOptional(CstExpression) {
			cstLeafNode(TokenKinds.Operator.Assign.Assign, "=")
			cstExpression()
		}
	}
	
	val accessors = items { cstAccessorFunction() }
	
	CstVariable.Normal(initializer, CstDeclarations(accessors))
}

fun CstParseContext.cstStandaloneVariable(): CstVariable = structuredNode(CstStandaloneVariable) {
	CstStandaloneVariable(
		annotations = cstAnnotations(),
		context = cstContextDeclarationOrNone(),
		modifiers = cstModifiers(), // public open abstract ...
		kind = cstVariableKind(),
		name = cstIdentifier(),
		type = cstDeclarationQuoteTypeOrNone(),
		accessor = cstVariableAccessor(),
	)
}
