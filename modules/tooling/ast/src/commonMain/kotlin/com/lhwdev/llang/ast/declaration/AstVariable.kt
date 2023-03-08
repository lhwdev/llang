package com.lhwdev.llang.ast.declaration

import com.lhwdev.llang.common.BodyOmissionKind
import com.lhwdev.llang.common.Modality
import com.lhwdev.llang.common.Visibility


sealed interface AstVariable : AstCodeDeclaration {
	override val annotations: List<AstAnnotation>
	
	override val visibility: Visibility
	
	override val bodyOmission: BodyOmissionKind?
	
	override val name: String
	
	
}


interface AstBasicVariable : AstVariable, AstTopLevelDeclaration, AstLocalDeclaration

interface AstMemberVariable : AstVariable, AstMemberDeclaration {
	override val modality: Modality
}
