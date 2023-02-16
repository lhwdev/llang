package com.lhwdev.llang.lexer.code


interface CodeIterator {
	val following: CharSequence
}


inline val CodeIterator.eof: Boolean
	get() = following.isEmpty()

inline val CodeIterator.current: Char
	get() = following[0]

fun CodeIterator.ahead(index: Int = 1): Char =
	following.getOrElse(index) { '\u0000' }

fun CodeIterator.matchesNext(text: String): Boolean {
	if(following.length < text.length) return false
	
	@Suppress("ReplaceManualRangeWithIndicesCalls")
	for(i in 0 until text.length) {
		if(text[i] != following[i]) return false
	}
	
	return true
}

fun CodeIterator.matchesNext(text: String, offset: Int): Boolean {
	if(following.length + offset < text.length) return false
	
	@Suppress("ReplaceManualRangeWithIndicesCalls")
	for(i in 0 until text.length) {
		if(text[i] != following[i + offset]) return false
	}
	
	return true
}
