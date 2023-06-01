package com.lhwdev.llang.source


interface MutableCodeSource : CodeSource {
	fun advance(count: Int = 1)
}


fun MutableCodeSource.advanceToEolAhead() {
	while(!eof && !(current == '\n' || current == '\r')) {
		advance()
	}
}

inline fun MutableCodeSource.advanceWhile(condition: MutableCodeSource.() -> Boolean) {
	while(!eof && condition()) {
		advance()
	}
}
