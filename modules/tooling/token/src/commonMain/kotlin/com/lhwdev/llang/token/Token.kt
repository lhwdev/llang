package com.lhwdev.llang.token


class TokenStateKey<T>(val defaultValue: T, val debugName: String? = null) {
	override fun toString(): String = debugName ?: super.toString()
}


class Token(override var kind: TokenKind, override val code: String) : CstToken {
	override fun equals(other: Any?): Boolean = when {
		this === other -> true
		other !is Token -> false
		else -> kind == other.kind && code == other.code
	}
	
	override fun hashCode(): Int = kind.hashCode() * 31 + code.hashCode()
	
	override fun toString(): String = "Token(kind=$kind, code=$code)"
}
