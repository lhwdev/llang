package com.lhwdev.llang.token


// https://github.com/JetBrains/kotlin/tree/master/compiler/psi/src/org/jetbrains/kotlin/lexer


enum class Separator {
	Left, Right, Both
}


abstract class Token(val debugName: String) {
	override fun toString(): String = debugName
}

abstract class TokenSet(debugName: String) : Token(debugName) {
	abstract val tokens: List<Token>
}

abstract class TokenSetBuilder(debugName: String) : TokenSet(debugName) {
	private val mTokens = mutableListOf<Token>()
	
	override val tokens: List<Token>
		get() = mTokens
	
	fun <T : Token> token(token: T): T = token.also { mTokens += token }
	// fun tokenSet()
}

context(TokenSetBuilder)
@Suppress("NOTHING_TO_INLINE")
inline operator fun <T : Token> T.unaryPlus(): T = token(this)
