package com.lhwdev.llang.tokenizer

interface TokenizerContext {
	val location: ParseLocation
}

sealed class ParseLocation {
	/**
	 * Global or inside class.
	 */
	object Declarations : ParseLocation()
	
	/**
	 * Inside BlockBody.
	 */
	object Statements : ParseLocation()
	
	/**
	 * Inside ExpressionBody, defaultValue, parameter, operand etc.
	 */
	object Expression : ParseLocation()
	
	/**
	 * Inside string literal.
	 */
	sealed class StringLiteral : ParseLocation() {
		object Escaped : StringLiteral()
		object Raw : StringLiteral()
	}
	
	/**
	 * Inside comment.
	 */
	sealed class Comment : ParseLocation() {
		object Eol : Comment()
		object Block : Comment()
		object LDocBlock : Comment()
	}
}
