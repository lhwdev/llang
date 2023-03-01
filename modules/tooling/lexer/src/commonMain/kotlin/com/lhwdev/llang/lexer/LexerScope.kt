package com.lhwdev.llang.lexer

import com.lhwdev.llang.diagnostic.Diagnostic
import com.lhwdev.llang.lexer.code.MutableCodeIterator
import com.lhwdev.llang.token.LlTokenKind
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.token.TokenStateKey


sealed class LexerIndex {
	class Code(val index: Int) : LexerIndex()
	class Token(val index: Int) : LexerIndex()
}


interface LexerScope : MutableCodeIterator {
	fun markStart()
	
	fun buildToken(token: LlTokenKind): Token // returned tokens are not interned anywhere; this is simple lightweight utility
	
	val currentIndex: LexerIndex
	
	val currentSpan: CharSequence
	
	
	/**
	 * Note: cannot push multiple state at one token so far
	 */
	fun <T> pushState(key: TokenStateKey<T>, value: T)
	
	fun <T> popState(key: TokenStateKey<T>): T
	
	fun <T> getCurrentState(key: TokenStateKey<T>): T
	
	
	fun pushDiagnostic(diagnostic: Diagnostic, index: LexerIndex = currentIndex)
}

context(LexerScope)
val <T> TokenStateKey<T>.value: T
	get() = getCurrentState(this)


fun LexerScope.token(token: LlTokenKind, length: Int = 1): Token = token(token) { advance(length) }

inline fun LexerScope.token(token: LlTokenKind, advanceBlock: () -> Unit): Token {
	markStart()
	advanceBlock()
	return buildToken(token)
}

inline fun LexerScope.token(advanceBlock: () -> LlTokenKind): Token {
	markStart()
	return buildToken(advanceBlock())
}

fun LexerScope.illegalToken(length: Int = 1, reason: String? = null): Token =
	token(TokenKinds.Illegal(reason))
