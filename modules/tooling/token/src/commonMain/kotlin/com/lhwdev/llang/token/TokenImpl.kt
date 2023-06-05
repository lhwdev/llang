package com.lhwdev.llang.token


class TokenImpl(override var kind: TokenKind, override val code: String) : Token {
	override fun equals(other: Any?): Boolean = when {
		this === other -> true
		other !is TokenImpl -> false
		else -> kind == other.kind && code == other.code
	}
	
	override fun hashCode(): Int = kind.hashCode() * 31 + code.hashCode()
	
	override fun toString(): String = "Token(kind=$kind, code=$code)"
}
