package com.lhwdev.llang.parser.declaration

import com.lhwdev.llang.cst.structure.declaration.CstDeclarations
import com.lhwdev.llang.cst.structure.declaration.CstStandaloneVariable
import com.lhwdev.llang.cst.structure.declaration.CstVariable
import com.lhwdev.llang.cst.structure.declaration.CstVariableKind
import com.lhwdev.llang.cst.structure.util.CstOptional
import com.lhwdev.llang.parser.*
import com.lhwdev.llang.parser.core.*
import com.lhwdev.llang.parser.expression.cstExpression
import com.lhwdev.llang.parser.statement.cstStatements
import com.lhwdev.llang.parser.type.cstDeclarationQuoteTypeOrNone
import com.lhwdev.llang.parser.util.items
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.parseVariableKind


private fun CstParseContext.cstVariableKind(): CstVariableKind = keywordLeafNode(CstVariableKind) {
	CstVariableKind(code.parseVariableKind())
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
	val initializer = itemOrNull {
		cstLeafNode(TokenKinds.Operator.Assign.Assign, "=")
		cstExpression()
	}
	
	val accessors = items { cstAccessorFunction() }
	
	CstVariable.Normal(CstOptional(initializer), CstDeclarations(accessors))
}

fun CstParseContext.cstStandaloneVariable(): CstVariable = structuredNode(CstStandaloneVariable) {
	CstStandaloneVariable(
		annotations = cstAnnotations(),
		context = cstContextDeclarationOrNone(),
		modifiers = cstModifiers(), // public open abstract ...
		kind = cstVariableKind(),
		extensionReceiverParameter = cstExtensionReceiverParameterOrNone(),
		name = cstIdentifier(),
		type = cstDeclarationQuoteTypeOrNone(),
		accessor = cstVariableAccessor(),
	)
}
