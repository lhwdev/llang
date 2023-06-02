package com.lhwdev.llang.cst


interface CstLocalContextKey<T> {
	context(CstCodeSource)
	val defaultValue: T
}


class CstLocalContext(
	val parent: CstLocalContext? = null,
	val location: Location,
) {
	sealed class Location {
		/**
		 * Global or inside class.
		 */
		object Declarations : Location()
		
		/**
		 * Inside BlockBody.
		 */
		object Statements : Location()
		
		/**
		 * Inside ExpressionBody, defaultValue, parameter, operand etc.
		 */
		object Expression : Location()
		
		/**
		 * Inside string literal.
		 */
		sealed class StringLiteral : Location() {
			object Escaped : StringLiteral()
			object Raw : StringLiteral()
		}
		
		/**
		 * Inside comment.
		 */
		sealed class Comment : Location() {
			object Eol : Comment()
			object Block : Comment()
			object LDocBlock : Comment()
		}
	}
}
