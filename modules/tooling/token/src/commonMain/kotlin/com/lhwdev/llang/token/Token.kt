package com.lhwdev.llang.token


class TokenStateKey<T>(val defaultValue: T, val debugName: String? = null) {
	override fun toString(): String = debugName ?: super.toString()
}


sealed class Token(var kind: TokenKind, val code: String) : CstToken {
	class Plain(kind: TokenKind, code: String) : Token(kind, code) {
		override fun equals(other: Any?): Boolean =
			other is Plain && super.equals(other)
		
		override fun hashCode(): Int = super.hashCode() + 1
		
		override fun toString(): String = "$kind $code"
	}
	
	class PushState(
		kind: TokenKind,
		code: String,
		val stateKey: TokenStateKey<*>,
		val stateValue: Any?
	) : Token(kind, code) {
		override fun equals(other: Any?): Boolean =
			other is PushState && super.equals(other) &&
				stateKey == other.stateKey && stateValue == other.stateValue
		
		override fun hashCode(): Int =
			(super.hashCode() * 31 + stateKey.hashCode()) * 31 + stateValue.hashCode()
		
		override fun toString(): String = "$kind $code (+PushState $stateKey=$stateValue)"
	}
	
	class PopState(
		kind: TokenKind,
		code: String,
		val stateKey: TokenStateKey<*>
	) : Token(kind, code) {
		override fun equals(other: Any?): Boolean =
			other is PopState && super.equals(other) &&
				stateKey == other.stateKey
		
		override fun hashCode(): Int =
			super.hashCode() * 31 + stateKey.hashCode()
		
		override fun toString(): String = "$kind $code (+PopState $stateKey)"
	}
	
	
	override fun equals(other: Any?): Boolean = when {
		this === other -> true
		other !is Token -> false
		else -> kind == other.kind && code == other.code
	}
	
	override fun hashCode(): Int = kind.hashCode() * 31 + code.hashCode()
}
