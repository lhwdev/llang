package com.lhwdev.llang.ast.type

import com.lhwdev.llang.ast.AstNode
import com.lhwdev.llang.ast.expression.AstTypeArgument


interface AstType : AstNode {
	interface Exact : AstType {
		val classifier: AstTypeReference
		
		val typeArguments: List<AstTypeArgument>
	}
	
	interface Intersects : AstType {
		val left: AstType
		val right: AstType
	}
	
	interface Union : AstType {
		val left: AstType
		val right: AstType
	}
}
