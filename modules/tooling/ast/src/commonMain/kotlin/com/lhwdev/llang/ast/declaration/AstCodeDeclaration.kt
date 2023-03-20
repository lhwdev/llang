package com.lhwdev.llang.ast.declaration

import com.lhwdev.llang.ast.AstNamed
import com.lhwdev.llang.common.BodyOmissionKind
import com.lhwdev.llang.common.Visibility


interface AstCodeDeclaration : AstDeclaration, AstNamed, AstAnnotatable {
	override val annotations: List<AstAnnotation>
	
	override val name: String
	
	val bodyOmission: BodyOmissionKind?
}


interface AstCodeDeclarationWithVisibility : AstCodeDeclaration {
	val visibility: Visibility
}
