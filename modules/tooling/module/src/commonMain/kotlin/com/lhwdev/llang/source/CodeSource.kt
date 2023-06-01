package com.lhwdev.llang.source


interface CodeSource {
	val next: CodeSequence
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

