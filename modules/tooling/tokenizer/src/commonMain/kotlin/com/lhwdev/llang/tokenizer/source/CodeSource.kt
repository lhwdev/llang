package com.lhwdev.llang.tokenizer.source

import com.lhwdev.llang.parsing.ParseContext
import com.lhwdev.llang.parsing.discard
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKind
import com.lhwdev.llang.tokenizer.CharacterKind


interface CodeSource : ParseContext {
	val next: CodeSequence
	
	
	/// Token
	
	val currentSpan: CharSequence
	
	fun advance(count: Int = 1)
	
	fun buildToken(kind: TokenKind): Token
	
	fun resetToSpanStart()
	
	fun hiddenDebugCommands(command: String, vararg args: Any?): Any? {
		return null
	}
}

inline val CodeSource.eof: Boolean
	get() = next.isEmpty()

inline val CodeSource.current: Char
	get() = next[0]

fun CodeSource.advanceOne(): Char {
	val current = current
	advance()
	return current
}

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

fun CodeSource.matchesWord(text: String, offset: Int = 0): Boolean {
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
		discard { "expected $char, but encountered $current" }
	}
	
	advance()
}

fun CodeSource.matchesAdvance(char: Char): Boolean =
	if(current == char) {
		advance()
		true
	} else {
		false
	}

inline fun CodeSource.advanceMatch(block: CodeSource.() -> Boolean) {
	if(!block()) {
		discard { "got $current" }
	}
	
	advance()
}

fun CodeSource.advanceMatch(text: String) {
	if(!matches(text)) {
		discard { "expected $text, but encountered ${next.substring(0, text.length)}" }
	}
	
	advance(text.length)
}

fun CodeSource.matchesAdvance(text: String): Boolean =
	if(matches(text)) {
		advance(text.length)
		true
	} else {
		false
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

fun CodeSource.advanceInWordNotEmpty(): CharSequence {
	advanceMatch { CharacterKind.isLetter(current) }
	advanceWhile { CharacterKind.isIdentifier(current) }
	requireNotEmpty()
	return currentSpan
}
