package com.lhwdev.llang.tokenizer.source

import com.lhwdev.llang.parsing.util.ParseContext
import com.lhwdev.llang.parsing.util.parseError
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKind
import com.lhwdev.llang.tokenizer.CharacterKind
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

fun CodeSource.match(text: String, offset: Int = 0): Boolean {
	val next = next
	if(next.length + offset < text.length) return false
	
	for(i in text.indices) {
		if(text[i] != next[i + offset]) return false
	}
	return true
}

fun CodeSource.matchWord(text: String, offset: Int = 0): Boolean {
	val startOffset = currentSpan.length
	var index = 0
	if(!CharacterKind.isLetter(next[index]))
		return false
	
	index++
	
	while(CharacterKind.isIdentifier(next[index]) && index <= text.length) {
		index++
	}
	
	if(text.length != index)
		return false
	
	for(i in 0 until index) {
		if(text[i] != currentSpan[startOffset + i]) return false
	}
	return true
}

fun CodeSource.advanceMatch(char: Char) {
	if(current != char) {
		parseError("expected $char, but encountered $current")
	}
	
	advance()
}

inline fun CodeSource.advanceMatch(block: CodeSource.() -> Boolean) {
	if(!block()) {
		parseError("got $current")
	}
	
	advance()
}

fun CodeSource.advanceMatch(text: String) {
	if(!match(text)) {
		parseError("expected $text, but encountered ${next.substring(0, text.length)}")
	}
	
	advance(text.length)
}

fun CodeSource.advanceToAheadEol() {
	while(!eof && !(current == '\n' || current == '\r')) {
		advance()
	}
}

inline fun CodeSource.advanceWhile(condition: CodeSource.() -> Boolean) {
	while(!eof && condition()) {
		advance()
	}
}
