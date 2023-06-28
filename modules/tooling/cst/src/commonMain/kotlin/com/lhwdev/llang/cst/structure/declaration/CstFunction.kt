package com.lhwdev.llang.cst.structure.declaration

import com.lhwdev.llang.cst.structure.core.CstIdentifier
import com.lhwdev.llang.cst.structure.core.CstModifiers
import com.lhwdev.llang.cst.structure.type.CstType
import com.lhwdev.llang.cst.structure.util.CstOptional


open class CstFunction(
	final override val annotations: CstAnnotations,
	
	val context: CstOptional<CstContextDeclaration>,
	
	final override val modifiers: CstModifiers,
	
	val extensionReceiverParameter: CstOptional<CstExtensionReceiverParameter>,
	
	final override val name: CstIdentifier,
	
	val typeParameters: CstOptional<CstTypeParameters>,
	
	val valueParameters: CstValueParameters,
	
	val returnType: CstOptional<CstType>,
	
	val typeParameterConstraints: CstOptional<CstTypeParameterConstraints>,
	
	val body: CstOptional<CstBody>,
) : CstNamedDeclaration


class CstAccessorFunction(
	annotations: CstAnnotations,
	context: CstOptional<CstContextDeclaration>,
	modifiers: CstModifiers,
	extensionReceiverParameter: CstOptional<CstExtensionReceiverParameter>,
	name: CstIdentifier,
	typeParameters: CstOptional<CstTypeParameters>,
	valueParameters: CstValueParameters,
	returnType: CstOptional<CstType>,
	typeParameterConstraints: CstOptional<CstTypeParameterConstraints>,
	body: CstOptional<CstBody>,
) : CstFunction(
	annotations,
	context,
	modifiers,
	extensionReceiverParameter,
	name,
	typeParameters,
	valueParameters,
	returnType,
	typeParameterConstraints,
	body,
)
