package com.lhwdev.llang.token


// https://github.com/JetBrains/kotlin/tree/master/compiler/psi/src/org/jetbrains/kotlin/lexer


enum class Separator {
	Left, Right, Both
}


abstract class TokenKind(val debugName: String) {
	open val isSeparator: Boolean
		get() = false
	
	override fun toString(): String = debugName
}

abstract class TokenKindSet(debugName: String) : TokenKind(debugName) {
	abstract val kinds: List<TokenKind>
}

abstract class TokenKindSetBuilder(debugName: String) : TokenKindSet(debugName) {
	private val mTokenKinds = mutableListOf<TokenKind>()
	
	override val kinds: List<TokenKind>
		get() = mTokenKinds
	
	fun <T : TokenKind> token(token: T): T = token.also { mTokenKinds += token }
	// fun tokenSet()
}

context(TokenKindSetBuilder)
@Suppress("NOTHING_TO_INLINE")
inline operator fun <T : TokenKind> T.unaryPlus(): T = token(this)
