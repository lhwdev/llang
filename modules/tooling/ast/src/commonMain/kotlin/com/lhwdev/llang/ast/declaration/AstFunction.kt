package com.lhwdev.llang.ast.declaration

import com.lhwdev.llang.ast.reference.AstTypeReference
import com.lhwdev.llang.common.BodyOmissionKind
import com.lhwdev.llang.common.Modality
import com.lhwdev.llang.common.Visibility


sealed interface AstFunction : AstCodeDeclaration {
	override val annotations: List<AstAnnotation>
	
	override val visibility: Visibility
	
	override val bodyOmission: BodyOmissionKind?
	
	// val modality: Modality // only on AstMemberFunction
	
	override val name: String
	
	val valueParameters: List<AstValueParameter.Simple>
	
	@AstInferable
	val returnType: AstTypeReference
	
	val body: AstBody
}


// top-level, local, lambda, member
sealed interface AstSimpleFunction : AstFunction {
	val contextReceivers: List<AstValueParameter.ContextReceiver>
	
	@AstBuiltinColor
	val isInline: Boolean
	
	@AstBuiltinColor
	val isSuspend: Boolean
	
	val extensionReceiver: AstValueParameter.ExtensionReceiver?
	
	val typeParameters: List<AstTypeParameter>
}

// top-level, local, member, constructor
interface AstDeclaredFunction : AstFunction {
	val isInfix: Boolean
}

// top-level, local, member
interface AstBasicFunction : AstSimpleFunction {
	override val valueParameters: List<AstValueParameter.SimpleDeclared>
}

interface AstLambdaFunction : AstSimpleFunction {
	override val valueParameters: List<AstValueParameter.SimpleLambda>
	
	override val isInfix: Boolean
		get() = false
}

// TODO: WIP, see excel file!

sealed interface AstMemberFunction : AstFunction, AstMemberDeclaration {
	override val modality: Modality
	
	val dispatchReceiver: AstValueParameter.DispatchReceiver
}


interface AstBasicMemberFunction : AstMemberFunction, AstDeclaredFunction {
	val isOverride: Boolean
}

interface AstConstructor : AstMemberFunction {
	val inPlaceVariables: List<AstMemberVariable>
}
