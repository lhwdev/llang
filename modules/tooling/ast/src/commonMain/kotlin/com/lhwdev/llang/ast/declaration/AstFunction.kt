package com.lhwdev.llang.ast.declaration

import com.lhwdev.llang.ast.AstNamed
import com.lhwdev.llang.ast.expression.AstTypeConstraint
import com.lhwdev.llang.ast.type.AstType
import com.lhwdev.llang.common.BodyOmissionKind
import com.lhwdev.llang.common.Modality
import com.lhwdev.llang.common.Visibility


/*
 * If union type like `Boolean | Implicit` were available, I could get rid of these all messes, merge
 * AstInferredSimpleFunction into AstFunction...
 */

interface AstFunction : AstDeclaration {
	val visibility: Visibility
}

sealed interface AstInferableFunction : AstFunction


// top-level, local, lambda, member (not constructor)
sealed interface AstSimpleFunction : AstFunction


// top-level, local, member, constructor (not lambda)
interface AstDeclaredFunction : AstInferableFunction, AstCodeDeclarationWithVisibility, AstNamed,
	AstAnnotatable {
	override val annotations: List<AstAnnotation>
	
	override val visibility: Visibility
	
	override val name: String
	
	val valueParameters: List<AstValueParameter.SimpleDeclared>
	
	val returnType: AstType
	
	val body: AstBody?
	
	override val bodyOmission: BodyOmissionKind?
}


// top-level, local, member (not lambda, constructor)
interface AstDeclaredSimpleFunction : AstSimpleFunction, AstDeclaredFunction {
	@AstBuiltinColor
	val isInline: Boolean
	
	@AstBuiltinColor
	val isSuspend: Boolean
	
	val contextReceivers: List<AstValueParameter.ContextReceiver>
	
	val extensionReceiver: AstValueParameter.ExtensionReceiver?
	
	val typeParameters: List<AstTypeParameter>
	
	val typeConstraints: List<AstTypeConstraint>
	
	val isInfix: Boolean
}


// lambda
interface AstInferredFunction : AstInferableFunction {
	val annotations: PartiallyInferable<List<AstAnnotation>>
	
	val valueParameters: Inferable<List<Inferable<AstValueParameter.SimpleInferred>>>
	
	val returnType: Inferable<AstType>
	
	val body: AstBody?
}

// lambda
interface AstInferredSimpleFunction : AstSimpleFunction, AstInferredFunction {
	@AstBuiltinColor
	val isInline: Implicit<Boolean>
		get() = Inferable.Implicit
	
	@AstBuiltinColor
	val isSuspend: Implicit<Boolean>
		get() = Inferable.Implicit
	
	val typeParameters: Implicit<List<AstTypeParameter>>
		get() = Inferable.Implicit
	
	val typeConstraints: Implicit<List<AstTypeConstraint>>
		get() = Inferable.Implicit
	val contextReceivers: Implicit<List<AstValueParameter.ContextReceiver>>
		get() = Inferable.Implicit
	
	val extensionReceiver: Implicit<AstValueParameter.ExtensionReceiver?>
		get() = Inferable.Implicit
}


// top-level, local
interface AstStandaloneFunction : AstDeclaredFunction


// member, constructor
interface AstMemberFunction : AstDeclaredFunction, AstMemberDeclaration {
	override val modality: Modality
	
	val dispatchReceiver: AstValueParameter.DispatchReceiver
}


/// actual end function

interface AstTopLevelFunction : AstDeclaredSimpleFunction, AstStandaloneFunction,
	AstTopLevelDeclaration {
	override val annotations: List<AstAnnotation>
	override val contextReceivers: List<AstValueParameter.ContextReceiver>
	
	override val visibility: Visibility
	override val bodyOmission: BodyOmissionKind?
	override val isSuspend: Boolean
	override val isInline: Boolean
	override val isInfix: Boolean
	
	// `fun`
	
	override val typeParameters: List<AstTypeParameter>
	override val typeConstraints: List<AstTypeConstraint>
	
	override val extensionReceiver: AstValueParameter.ExtensionReceiver?
	override val name: String
	override val valueParameters: List<AstValueParameter.SimpleDeclared>
	override val returnType: AstType
	
	override val body: AstBody?
}

interface AstLocalFunction : AstDeclaredSimpleFunction, AstStandaloneFunction, AstLocalDeclaration {
	override val annotations: List<AstAnnotation>
	override val contextReceivers: List<AstValueParameter.ContextReceiver>
	
	override val visibility: Visibility
		get() = Visibility.Local
	override val bodyOmission: BodyOmissionKind?
	override val isSuspend: Boolean
	override val isInline: Boolean
	override val isInfix: Boolean
	
	// `fun`
	
	override val typeParameters: List<AstTypeParameter>
	override val typeConstraints: List<AstTypeConstraint>
	
	override val extensionReceiver: AstValueParameter.ExtensionReceiver?
	override val name: String
	override val valueParameters: List<AstValueParameter.SimpleDeclared>
	override val returnType: AstType
	
	override val body: AstBody?
}

interface AstLambdaFunction : AstInferredSimpleFunction {
	override val visibility: Visibility
		get() = Visibility.Hidden
	
	override val annotations: PartiallyInferable<List<AstAnnotation>>
	override val contextReceivers: Implicit<List<AstValueParameter.ContextReceiver>>
		get() = Inferable.Implicit
	
	override val isSuspend: Implicit<Boolean>
		get() = Inferable.Implicit
	override val isInline: Implicit<Boolean>
		get() = Inferable.Implicit
	
	// `fun`
	
	override val typeParameters: Implicit<List<AstTypeParameter>>
		get() = Inferable.Implicit
	override val typeConstraints: Implicit<List<AstTypeConstraint>>
		get() = Inferable.Implicit
	
	override val extensionReceiver: Implicit<AstValueParameter.ExtensionReceiver?>
		get() = Inferable.Implicit
	override val name: String
	override val valueParameters: Inferable<List<Inferable<AstValueParameter.SimpleInferred>>>
	override val returnType: Inferable<AstType>
	
	override val body: AstBody
}

interface AstNormalMemberFunction : AstDeclaredSimpleFunction, AstMemberFunction {
	override val dispatchReceiver: AstValueParameter.DispatchReceiver
	
	override val annotations: List<AstAnnotation>
	override val contextReceivers: List<AstValueParameter.ContextReceiver>
	
	override val visibility: Visibility
	override val bodyOmission: BodyOmissionKind?
	override val modality: Modality
	override val isSuspend: Boolean
	override val isInline: Boolean
	override val isInfix: Boolean
	
	// `fun`
	
	override val typeParameters: List<AstTypeParameter>
	override val typeConstraints: List<AstTypeConstraint>
	
	override val extensionReceiver: AstValueParameter.ExtensionReceiver?
	override val name: String
	override val valueParameters: List<AstValueParameter.SimpleDeclared>
	override val returnType: AstType
	
	override val body: AstBody?
}

interface AstConstructor : AstDeclaredFunction, AstMemberFunction {
	override val dispatchReceiver: AstValueParameter.DispatchReceiver
	
	override val annotations: List<AstAnnotation>
	
	override val visibility: Visibility
	override val bodyOmission: BodyOmissionKind?
	override val modality: Modality
	
	// `fun`
	
	override val name: String
	override val valueParameters: List<AstValueParameter.SimpleDeclared>
	override val returnType: AstType
	
	override val body: AstBody?
}
