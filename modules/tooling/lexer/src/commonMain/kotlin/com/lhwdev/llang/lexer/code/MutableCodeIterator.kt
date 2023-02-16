package com.lhwdev.llang.lexer.code


interface MutableCodeIterator : CodeIterator {
	fun advance(count: Int = 1)
}

fun MutableCodeIterator.advanceBeforeEol() {
	while(!eof && !(current == '\n' || current == '\r')) {
		advance()
	}
}

inline fun MutableCodeIterator.advanceOneWhile(condition: MutableCodeIterator.() -> Boolean) {
	while(!eof && condition()) {
		advance()
	}
}
