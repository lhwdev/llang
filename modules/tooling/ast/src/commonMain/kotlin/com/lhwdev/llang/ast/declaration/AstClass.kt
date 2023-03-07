package com.lhwdev.llang.ast.declaration

import com.lhwdev.llang.common.ClassKind
import com.lhwdev.llang.common.Visibility


interface AstClass : AstDeclaration, AstLocalDeclaration, AstAnnotatable {
	override val annotations: List<AstAnnotation>
	
	val visibility: Visibility
	
	
	val classKind: ClassKind
	
	override val name: String
}
