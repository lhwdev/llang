package com.lhwdev.llang.parser.declaration

import com.lhwdev.llang.cst.structure.core.CstIdentifier
import com.lhwdev.llang.cst.structure.core.CstModifiers
import com.lhwdev.llang.cst.structure.declaration.*
import com.lhwdev.llang.cst.structure.util.CstOptional
import com.lhwdev.llang.cst.structure.util.optional
import com.lhwdev.llang.parser.*
import com.lhwdev.llang.parser.core.*
import com.lhwdev.llang.parser.type.cstDeclarationQuoteTypeOrNone
import com.lhwdev.llang.parser.type.cstType
import com.lhwdev.llang.parser.util.cstCommaSeparatedList
import com.lhwdev.llang.token.TokenImpl
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.source.advanceInWordNotEmpty
import com.lhwdev.llang.tokenizer.source.parseToken
import com.lhwdev.llang.tokenizer.source.token


fun CstParseContext.cstObjectFunction() = structuredNode {
	CstMemberFunction(
		annotations = cstAnnotations(),
		context = cstContextDeclarationOrNone(),
		modifiers = cstModifiers(),
		kind = cstFunKeyword(CstFunction.Kind.ObjectMember),
		extensionReceiverParameter = cstExtensionReceiverParameterOrNone(),
		name = cstIdentifier(),
		typeParameters = cstTypeParametersOrNone(),
		valueParameters = cstValueParameters(),
		returnType = cstDeclarationQuoteTypeOrNone(),
		typeParameterConstraints = cstTypeParameterConstraintsOrNone(),
		body = cstBodyOrNone(),
	)
}

fun CstParseContext.cstMemberFunction() = structuredNode {
	CstMemberFunction(
		annotations = cstAnnotations(),
		context = cstContextDeclarationOrNone(),
		modifiers = cstModifiers(),
		kind = cstFunKeyword(CstFunction.Kind.ClassMember),
		extensionReceiverParameter = cstExtensionReceiverParameterOrNone(),
		name = cstIdentifier(),
		typeParameters = cstTypeParametersOrNone(),
		valueParameters = cstValueParameters(),
		returnType = cstDeclarationQuoteTypeOrNone(),
		typeParameterConstraints = cstTypeParameterConstraintsOrNone(),
		body = cstBodyOrNone(),
	)
}

fun CstParseContext.cstLocalFunction() = structuredNode {
	CstLocalFunction(
		annotations = cstAnnotations(),
		context = cstContextDeclarationOrNone(),
		modifiers = cstModifiers(),
		kind = cstFunKeyword(CstFunction.Kind.Local),
		extensionReceiverParameter = cstExtensionReceiverParameterOrNone(),
		name = cstIdentifier(),
		typeParameters = cstTypeParametersOrNone(),
		valueParameters = cstValueParameters(),
		returnType = cstDeclarationQuoteTypeOrNone(),
		typeParameterConstraints = cstTypeParameterConstraintsOrNone(),
		body = cstBodyOrNone(),
	)
}

fun CstParseContext.cstConstructorFunction() = structuredNode {
	CstConstructorFunction(
		annotations = cstAnnotations(),
		context = cstContextDeclarationOrNone(),
		modifiers = cstModifiers(),
		kind = CstFunction.Kind.Constructor,
		extensionReceiverParameter = cstExtensionReceiverParameterOrNone(),
		name = keywordLeafNode {
			CstIdentifier(code.parseToken(TokenKinds.SoftKeyword.Constructor, "constructor"))
		},
		typeParameters = cstTypeParametersOrNone(),
		valueParameters = cstValueParameters(),
		returnType = cstDeclarationQuoteTypeOrNone(),
		typeParameterConstraints = cstTypeParameterConstraintsOrNone(),
		body = cstBodyOrNone(),
	)
}

fun CstParseContext.cstPrimaryConstructorFunction() = node {
	discardable {
		CstConstructorFunction(
			annotations = CstAnnotations(emptyList()),
			context = CstOptional.None,
			modifiers = CstModifiers(emptyList()),
			kind = CstFunction.Kind.Constructor,
			extensionReceiverParameter = cstExtensionReceiverParameterOrNone(),
			name = CstIdentifier(TokenImpl.synthetic(TokenKinds.SoftKeyword.Constructor, "")),
			typeParameters = CstOptional.None,
			valueParameters = cstValueParameters(),
			returnType = CstOptional.None,
			typeParameterConstraints = CstOptional.None,
			body = CstOptional.None,
		)
	} ?: CstConstructorFunction(
		annotations = cstAnnotations(),
		context = cstContextDeclarationOrNone(),
		modifiers = cstModifiers(),
		kind = CstFunction.Kind.Constructor,
		extensionReceiverParameter = cstExtensionReceiverParameterOrNone(),
		name = leafNode {
			CstIdentifier(code.parseToken(TokenKinds.SoftKeyword.Constructor, "constructor"))
		},
		typeParameters = CstOptional.None,
		valueParameters = cstValueParameters(),
		returnType = CstOptional.None,
		typeParameterConstraints = CstOptional.None,
		body = CstOptional.None,
	)
	
}

fun CstParseContext.cstAccessorFunction() = structuredNode {
	CstAccessorFunction(
		annotations = cstAnnotations(),
		context = cstContextDeclarationOrNone(),
		modifiers = cstModifiers(),
		kind = cstFunKeyword(CstFunction.Kind.Accessor),
		extensionReceiverParameter = cstExtensionReceiverParameterOrNone(),
		name = leafNode {
			val token = code.token {
				when(advanceInWordNotEmpty()) {
					"get" -> TokenKinds.SoftKeyword.Get
					"set" -> TokenKinds.SoftKeyword.Set
					else -> discard()
				}
			}
			CstIdentifier(token)
		},
		typeParameters = cstTypeParametersOrNone(),
		valueParameters = cstValueParameters(),
		returnType = cstDeclarationQuoteTypeOrNone(),
		typeParameterConstraints = cstTypeParameterConstraintsOrNone(),
		body = cstBodyOrNone(),
	)
}


/// Function Components

private fun CstParseContext.cstFunKeyword(kind: CstFunction.Kind): CstFunction.Kind {
	cstKeyword(TokenKinds.Keyword.Fun, "fun")
	return kind
}

private fun CstParseContext.cstTypeParameterConstraintsOrNone(): CstOptional<CstTypeParameterConstraints> =
	nullableStructuredNode {
		cstSoftKeywordOrNull(TokenKinds.SoftKeyword.Where, "where")
			?: return@nullableStructuredNode null
		
		CstTypeParameterConstraints(
			constraints = cstCommaSeparatedList {
				cstTypeParameterConstraint()
			}.items(),
		)
	}.optional

private fun CstParseContext.cstTypeParameterConstraint() =
	structuredNode {
		val target = cstType()
		cstLeafColonOrNull()!!
		val constraint = cstType()
		
		CstTypeParameterConstraint(target, constraint)
	}
