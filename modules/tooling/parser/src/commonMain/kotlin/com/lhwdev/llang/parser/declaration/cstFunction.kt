package com.lhwdev.llang.parser.declaration

import com.lhwdev.llang.cst.structure.declaration.*
import com.lhwdev.llang.cst.structure.util.CstOptional
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.core.cstIdentifier
import com.lhwdev.llang.parser.core.cstModifiers
import com.lhwdev.llang.parser.structuredNode
import com.lhwdev.llang.parser.type.cstDeclarationQuoteTypeOrNone


fun CstParseContext.cstObjectFunction() = structuredNode(CstMemberFunction) {
	CstMemberFunction(
		kind = CstFunction.Kind.ObjectMember,
		annotations = cstAnnotations(),
		context = cstContextDeclarationOrNone(),
		modifiers = cstModifiers(),
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
		modifiers = cstModifiers(),
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
		modifiers = cstModifiers(),
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
		name = cstIdentifier(),
		typeParameters = cstTypeParametersOrNone(),
		valueParameters = cstValueParameters(),
		returnType = cstDeclarationQuoteTypeOrNone(),
		typeParameterConstraints = cstTypeParameterConstraintsOrNone(),
		body = cstBodyOrNone(),
	)
}


fun CstParseContext.cstAccessorFunction() = structuredNode(CstAccessorFunction) {
	CstAccessorFunction(
		kind = CstFunction.Kind.Accessor,
		annotations = cstAnnotations(),
		context = cstContextDeclarationOrNone(),
		modifiers = cstModifiers(),
		extensionReceiverParameter = cstExtensionReceiverParameterOrNone(),
		name = cstIdentifier(),
		typeParameters = cstTypeParametersOrNone(),
		valueParameters = cstValueParameters(),
		returnType = cstDeclarationQuoteTypeOrNone(),
		typeParameterConstraints = cstTypeParameterConstraintsOrNone(),
		body = cstBodyOrNone(),
	)
}


/// Function Components

private fun CstParseContext.cstTypeParameterConstraintsOrNone(): CstOptional<CstTypeParameterConstraints> {
	TODO("Not yet implemented")
}
