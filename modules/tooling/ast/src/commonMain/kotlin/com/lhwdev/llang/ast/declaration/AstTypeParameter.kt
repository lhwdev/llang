package com.lhwdev.llang.ast.declaration

import com.lhwdev.llang.ast.type.AstVariance


interface AstTypeParameter : AstCodeDeclaration {
	enum class Retention {
		Default, Referential, Erased, Inline
	}
	
	
	val retention: Retention // nothing specified = default
	
	override val name: String
	
	val variance: AstVariance // nothing specified = AstVariance.StarProjection
}
