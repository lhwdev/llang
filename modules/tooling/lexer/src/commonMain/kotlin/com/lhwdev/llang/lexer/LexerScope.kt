package com.lhwdev.llang.lexer

import com.lhwdev.llang.diagnostic.Diagnostic
import com.lhwdev.llang.lexer.code.MutableCodeIterator
import com.lhwdev.llang.token.LlToken
import com.lhwdev.llang.token.Span
import com.lhwdev.llang.token.SpanStateKey
import com.lhwdev.llang.token.Tokens


interface LexerScope : MutableCodeIterator {
	fun markStart()
	
	fun moveToStart()
	
	fun buildSpan(token: LlToken): Span // returned spans are not interned anywhere; this is simple lightweight utility
	
	val currentSpan: CharSequence
	
	
	/**
	 * Note: cannot push multiple state at one token so far
	 */
	fun <T> pushState(key: SpanStateKey<T>, value: T)
	
	fun <T> popState(key: SpanStateKey<T>): T
	
	fun <T> getCurrentState(key: SpanStateKey<T>): T
	
	
	fun pushDiagnostic(diagnostic: Diagnostic)
}

context(LexerScope)
val <T> SpanStateKey<T>.value: T
	get() = getCurrentState(this)


fun LexerScope.span(token: LlToken, length: Int = 1): Span = span(token) { advance(length) }

inline fun LexerScope.span(token: LlToken, advanceBlock: () -> Unit): Span {
	markStart()
	advanceBlock()
	return buildSpan(token)
}

inline fun LexerScope.span(advanceBlock: () -> LlToken): Span {
	markStart()
	return buildSpan(advanceBlock())
}

fun LexerScope.illegalSpan(length: Int = 1, reason: String? = null): Span =
	span(Tokens.Illegal(reason))
