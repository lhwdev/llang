package com.lhwdev.llang.ast.expression

import com.lhwdev.llang.ast.AstNode
import com.lhwdev.llang.ast.declaration.Inferable
import com.lhwdev.llang.ast.type.AstVariance


/**
 * Note that this is not only a constraint for specific type.
 */
interface AstTypeArgument : AstNode {
	/**
	 * A lexical order of type argument appearing in `< >`.
	 */
	val index: Int
	
	/**
	 * Optional explicit name. This is for associated type, as [AstTypeArgument] is also used for associated type.
	 */
	val name: Inferable<String>
	
	val variance: AstVariance
}


interface AstTypeConstraint : AstNode
