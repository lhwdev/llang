package com.lhwdev.llang.ast.declaration

import com.lhwdev.llang.ast.AstNamed
import com.lhwdev.llang.ast.reference.AstTypeReference
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


// top-level, local, lambda, member (not constructor)
sealed interface AstSimpleFunction : AstFunction


// top-level, local, member, constructor (not lambda)
interface AstDeclaredFunction : AstFunction, AstCodeDeclaration, AstNamed, AstAnnotatable {
	override val annotations: List<AstAnnotation>
	
	override val visibility: Visibility
	
	override val name: String
	
	val valueParameters: List<AstValueParameter.Simple>
	
	val returnType: AstTypeReference
	
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
	
	val isInfix: Boolean
}


// lambda
interface AstInferredSimpleFunction : AstSimpleFunction {
	val annotations: PartiallyInferable<List<AstAnnotation>>
	
	@AstBuiltinColor
	val isInline: Implicit<Boolean>
		get() = Inferable.Implicit
	
	@AstBuiltinColor
	val isSuspend: Implicit<Boolean>
		get() = Inferable.Implicit
	
	val typeParameters: Implicit<List<AstTypeParameter>>
		get() = Inferable.Implicit
	
	val contextReceivers: Implicit<List<AstValueParameter.ContextReceiver>>
		get() = Inferable.Implicit
	
	val extensionReceiver: Implicit<AstValueParameter.ExtensionReceiver?>
		get() = Inferable.Implicit
	
	val valueParameters: Inferable<List<Inferable<AstValueParameter.SimpleLambda>>>
	
	val returnType: Inferable<AstTypeReference>
	
	val body: AstBody?
}


// top-level, local
interface AstStandaloneFunction : AstDeclaredFunction


// member, constructor
interface AstMemberFunction : AstDeclaredFunction, AstMemberDeclaration {
	override val modality: Modality
	
	val dispatchReceiver: AstValueParameter.DispatchReceiver
}


/// actual end function

interface AstTopLevelFunction : AstDeclaredSimpleFunction, AstStandaloneFunction, AstTopLevelDeclaration {
	override val annotations: List<AstAnnotation>
	override val contextReceivers: List<AstValueParameter.ContextReceiver>
	
	override val visibility: Visibility
	override val bodyOmission: BodyOmissionKind?
	override val isSuspend: Boolean
	override val isInline: Boolean
	override val isInfix: Boolean
	
	// `fun`
	
	override val typeParameters: List<AstTypeParameter>
	
	override val extensionReceiver: AstValueParameter.ExtensionReceiver?
	override val name: String
	override val valueParameters: List<AstValueParameter.SimpleDeclared>
	override val returnType: AstTypeReference
	
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
	
	override val extensionReceiver: AstValueParameter.ExtensionReceiver?
	override val name: String
	override val valueParameters: List<AstValueParameter.SimpleDeclared>
	override val returnType: AstTypeReference
	
	override val body: AstBody?
}

interface AstLambdaFunction : AstInferredSimpleFunction {
	override val annotations: PartiallyInferable<List<AstAnnotation>>
	override val contextReceivers: Implicit<List<AstValueParameter.ContextReceiver>>
	
	override val isSuspend: Implicit<Boolean>
	override val isInline: Implicit<Boolean>
	
	// `fun`
	
	override val typeParameters: Implicit<List<AstTypeParameter>>
	
	override val extensionReceiver: Implicit<AstValueParameter.ExtensionReceiver?>
	override val name: String
	override val valueParameters: Inferable<List<Inferable<AstValueParameter.SimpleLambda>>>
	override val returnType: Inferable<AstTypeReference>
	
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
	
	override val extensionReceiver: AstValueParameter.ExtensionReceiver?
	override val name: String
	override val valueParameters: List<AstValueParameter.SimpleDeclared>
	override val returnType: AstTypeReference
	
	override val body: AstBody?
}
