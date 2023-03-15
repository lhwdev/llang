package com.lhwdev.llang.ast.reference

import com.lhwdev.llang.ast.AstNode
import com.lhwdev.llang.ast.expression.AstTypeArgument



interface AstTypeReference : AstNode {
	interface Solid : AstTypeReference {
		val typeArguments: List<AstTypeArgument>
	}
}
