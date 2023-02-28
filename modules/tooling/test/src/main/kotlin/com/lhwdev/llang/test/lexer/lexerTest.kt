package com.lhwdev.llang.test.lexer

import com.lhwdev.llang.lexer.LexerFrontend
import com.lhwdev.llang.module.LlangCode


fun main() {
	val code = """
	fun main() { // ho ya
		println("hello, ${'$'}name! ${'$'}{1 + 2}", a = 123 + 0x14) /*ho
		hi */
	}
	""".trimIndent()
	
	val llangCode = object : LlangCode, CharSequence by code {}
	
	val lexerFrontend = LexerFrontend(llangCode)
	lexerFrontend.tokens.forEach { println(it) }
}
