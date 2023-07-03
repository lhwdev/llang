package com.lhwdev.llang.parser.declaration

import com.lhwdev.llang.cst.structure.core.CstIdentifier
import com.lhwdev.llang.cst.structure.core.CstModifiers
import com.lhwdev.llang.cst.structure.declaration.*
import com.lhwdev.llang.cst.structure.util.CstOptional
import com.lhwdev.llang.parser.*
import com.lhwdev.llang.parser.core.cstIdentifier
import com.lhwdev.llang.parser.core.cstKeyword
import com.lhwdev.llang.parser.core.cstModifiers
import com.lhwdev.llang.parser.core.keywordLeafNode
import com.lhwdev.llang.parser.type.cstDeclarationQuoteTypeOrNone
import com.lhwdev.llang.token.TokenImpl
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.source.advanceInWordNotEmpty
import com.lhwdev.llang.tokenizer.source.parseToken
import com.lhwdev.llang.tokenizer.source.token


fun CstParseContext.cstObjectFunction() = structuredNode(CstMemberFunction) {
	CstMemberFunction(
		kind = CstFunction.Kind.ObjectMember,
		annotations = cstAnnotations(),
		context = cstContextDeclarationOrNone(),
		modifiers = cstModifiers().also {
			cstFunKeyword()
		},
		extensionReceiverParameter = cstExtensionReceiverParameterOrNone(),
		name = cstIdentifier(),
		typeParameters = cstTypeParametersOrNone(),
		valueParameters = cstValueParameters(),
		returnType = cstDeclarationQuoteTypeOrNone(),
		typeParameterConstraints = cstTypeParameterConstraintsOrNone(),
		body = cstBodyOrNone(),
	)
}

fun CstParseContext.cstMemberFunction() = structuredNode(CstMemberFunction) {
	CstMemberFunction(
		kind = CstFunction.Kind.ClassMember,
		annotations = cstAnnotations(),
		context = cstContextDeclarationOrNone(),
		modifiers = cstModifiers().also {
			cstFunKeyword()
		},
		extensionReceiverParameter = cstExtensionReceiverParameterOrNone(),
		name = cstIdentifier(),
		typeParameters = cstTypeParametersOrNone(),
		valueParameters = cstValueParameters(),
		returnType = cstDeclarationQuoteTypeOrNone(),
		typeParameterConstraints = cstTypeParameterConstraintsOrNone(),
		body = cstBodyOrNone(),
	)
}

fun CstParseContext.cstLocalFunction() = structuredNode(CstLocalFunction) {
	CstLocalFunction(
		kind = CstFunction.Kind.Local,
		annotations = cstAnnotations(),
		context = cstContextDeclarationOrNone(),
		modifiers = cstModifiers().also {
			cstFunKeyword()
		},
		extensionReceiverParameter = cstExtensionReceiverParameterOrNone(),
		name = cstIdentifier(),
		typeParameters = cstTypeParametersOrNone(),
		valueParameters = cstValueParameters(),
		returnType = cstDeclarationQuoteTypeOrNone(),
		typeParameterConstraints = cstTypeParameterConstraintsOrNone(),
		body = cstBodyOrNone(),
	)
}

fun CstParseContext.cstConstructorFunction() = structuredNode(CstConstructorFunction) {
	CstConstructorFunction(
		kind = CstFunction.Kind.Constructor,
		annotations = cstAnnotations(),
		context = cstContextDeclarationOrNone(),
		modifiers = cstModifiers(),
		extensionReceiverParameter = cstExtensionReceiverParameterOrNone(),
		name = keywordLeafNode(CstIdentifier) {
			CstIdentifier(code.parseToken(TokenKinds.SoftKeyword.Constructor, "constructor"))
		},
		typeParameters = cstTypeParametersOrNone(),
		valueParameters = cstValueParameters(),
		returnType = cstDeclarationQuoteTypeOrNone(),
		typeParameterConstraints = cstTypeParameterConstraintsOrNone(),
		body = cstBodyOrNone(),
	)
}

fun CstParseContext.cstPrimaryConstructorFunction() = node(CstConstructorFunction) {
	discardable {
		CstConstructorFunction(
			kind = CstFunction.Kind.Constructor,
			annotations = CstAnnotations(emptyList()),
			context = CstOptional.None,
			modifiers = CstModifiers(emptyList()),
			extensionReceiverParameter = cstExtensionReceiverParameterOrNone(),
			name = CstIdentifier(TokenImpl.synthetic(TokenKinds.SoftKeyword.Constructor, "")),
			typeParameters = CstOptional.None,
			valueParameters = cstValueParameters(),
			returnType = CstOptional.None,
			typeParameterConstraints = CstOptional.None,
			body = CstOptional.None,
		)
	} ?: CstConstructorFunction(
		kind = CstFunction.Kind.Constructor,
		annotations = cstAnnotations(),
		context = cstContextDeclarationOrNone(),
		modifiers = cstModifiers(),
		extensionReceiverParameter = cstExtensionReceiverParameterOrNone(),
		name = leafNode(CstIdentifier) {
			CstIdentifier(code.parseToken(TokenKinds.SoftKeyword.Constructor, "constructor"))
		},
		typeParameters = CstOptional.None,
		valueParameters = cstValueParameters(),
		returnType = CstOptional.None,
		typeParameterConstraints = CstOptional.None,
		body = CstOptional.None,
	)
	
}

fun CstParseContext.cstAccessorFunction() = structuredNode(CstAccessorFunction) {
	CstAccessorFunction(
		kind = CstFunction.Kind.Accessor,
		annotations = cstAnnotations(),
		context = cstContextDeclarationOrNone(),
		modifiers = cstModifiers().also {
			cstFunKeyword()
		},
		extensionReceiverParameter = cstExtensionReceiverParameterOrNone(),
		name = leafNode(CstIdentifier) {
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

private fun CstParseContext.cstFunKeyword() {
	cstKeyword(TokenKinds.Keyword.Fun, "fun")
}

private fun CstParseContext.cstTypeParameterConstraintsOrNone(): CstOptional<CstTypeParameterConstraints> {
	TODO("Not yet implemented")
}
