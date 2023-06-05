package com.lhwdev.llang.tokenizer.source

import com.lhwdev.llang.parsing.util.ParseContext
import com.lhwdev.llang.parsing.util.parseError
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKind
import com.lhwdev.llang.tokenizer.TokenizerContext


interface CodeSource : ParseContext {
	val next: CodeSequence
	
	
	/// Token
	
	val currentSpan: CharSequence
	
	fun advance(count: Int = 1)
	
	fun buildToken(kind: TokenKind): Token
	
	fun resetToSpanStart()
	
	
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

fun CodeSource.advanceMatches(text: String) {
	if(!matches(text)) {
		parseError("expected $text, but encountered ${next.substring(0, text.length)}")
	}
	
	advance(text.length)
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
