package com.lhwdev.llang.tokenizer.source

import com.lhwdev.llang.parsing.util.ParseContext
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKind
import com.lhwdev.llang.tokenizer.TokenizerContext


interface CodeSource : ParseContext {
	val next: CodeSequence
	
	// val unsafeIndex: Int
	//
	// val unsafeCode: CodeSequence
	
	
	/// Token
	
	val currentSpan: CharSequence
	
	fun advance(count: Int = 1)
	
	fun buildToken(kind: TokenKind): Token
	
	
	/// Context
	
	val context: TokenizerContext
}

inline val CodeSource.eof: Boolean
	get() = next.isEmpty()

inline val CodeSource.current: Char
	get() = next[0]

fun CodeSource.peek(index: Int = 1): Char =
	next.getOrElse(index) { '\u0000' }

fun CodeSource.matches(text: String, offset: Int = 0): Boolean {
	val next = next
	if(next.length + offset < text.length) return false
	
	for(i in text.indices) {
		if(text[i] != next[i + offset]) return false
	}
	return true
}

fun CodeSource.advanceToEolAhead() {
	while(!eof && !(current == '\n' || current == '\r')) {
		advance()
	}
}

inline fun CodeSource.advanceWhile(condition: CodeSource.() -> Boolean) {
	while(!eof && condition()) {
		advance()
	}
}
