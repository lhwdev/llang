package com.lhwdev.llang.ast.declaration

import com.lhwdev.llang.ast.expression.AstStatement
import com.lhwdev.llang.ast.reference.AstTypeReference


interface AstBody : AstStatementContainer {
	@AstInferable
	val returnType: AstTypeReference?
	
	override val statements: List<AstStatement>
}

interface AstExpressionBody : AstBody

/**
 * All block bodies including of function can have implicit return, like { 3 }.
 */
interface AstBlockBody : AstBody
