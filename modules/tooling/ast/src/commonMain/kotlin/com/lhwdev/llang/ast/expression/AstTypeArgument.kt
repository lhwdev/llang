package com.lhwdev.llang.ast.expression

import com.lhwdev.llang.ast.AstNode
import com.lhwdev.llang.ast.declaration.Inferable


interface AstTypeArgument : AstNode {
	/**
	 * A lexical order of type argument appearing in `< >`.
	 */
	val index: Int
	
	/**
	 *
	 */
	val name: Inferable<String>
	
	
}


interface AstTypeConstraint : AstNode
