package com.lhwdev.llang.ast.declaration

import com.lhwdev.llang.ast.AstNamed


interface AstTypeParameter : AstDeclaration, AstNamed {
	override val name: String
	
	val
}
