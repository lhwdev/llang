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
	
	val valueParameters: List<AstValueParameter>
	
	val returnType: AstTypeReference
	
	val body: AstBody
}


sealed interface AstSimpleFunction : AstFunction {
	val contextReceivers: List<AstValueParameter.ContextReceiver>
	
	@AstBuiltinColor
	val isInline: Boolean
	
	@AstBuiltinColor
	val isSuspend: Boolean
	
	val isInfix: Boolean
	
	val extensionReceiver: AstValueParameter.ExtensionReceiver?
	
	val typeParameters: List<AstTypeParameter>
}

interface AstBasicFunction : AstSimpleFunction, AstTopLevelDeclaration, AstLocalDeclaration


sealed interface AstMemberFunction : AstFunction, AstMemberDeclaration {
	override val modality: Modality
	
	val dispatchReceiver: AstValueParameter.DispatchReceiver
}


interface AstSimpleMemberFunction : AstMemberFunction, AstSimpleFunction {
	val isOverride: Boolean
}

interface AstConstructor : AstMemberFunction {
	val inPlaceVariables: List<AstMemberVariable>
}
