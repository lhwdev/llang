package com.lhwdev.llang.token


class TokenStateKey<T>(val defaultValue: T, val debugName: String? = null) {
	override fun toString(): String = debugName ?: super.toString()
}


sealed class Token(var kind: TokenKind, val code: String) {
	class Plain(kind: TokenKind, code: String) : Token(kind, code) {
		override fun toString(): String = "$kind $code"
	}
	
	class PushState(
		kind: TokenKind,
		code: String,
		val stateKey: TokenStateKey<*>,
		val stateValue: Any?
	) : Token(kind, code) {
		override fun toString(): String = "$kind $code (+PushState $stateKey=$stateValue)"
	}
	
	class PopState(
		kind: TokenKind,
		code: String,
		val stateKey: TokenStateKey<*>
	) : Token(kind, code) {
		override fun toString(): String = "$kind $code (+PopState $stateKey)"
	}
	
}
