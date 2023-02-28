package com.lhwdev.llang.lexer

import com.lhwdev.llang.module.LlangCode
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.utils.collection.CollectionRange


class CodeModification(
	val oldCodeSpan: CollectionRange,
	val oldTokenSpan: CollectionRange,
	val insertedCode: CharSequence
)


class LexerFrontend(code: LlangCode) {
	private val backend = Lexer(code)
	
	val code: LlangCode
		get() = backend.code
	
	val tokens: List<Token>
		get() = backend.tokens
	
	init {
		parseInitially()
	}
	
	private fun parseInitially() {
		val scope = LexerScopeOnInitialization(backend)
		val run = LexerRun(scope)
		
		val list = ArrayList<Token>()
		while(true) {
			val token = run.advance()
			if(token.kind == TokenKinds.Eof) break
			list += token
		}
		backend.tokens = list
	}
	
	fun updateCode(newCode: LlangCode) {
		TODO()
	}
	
	fun updateCode(modification: CodeModification, newCode: LlangCode) {
		val backend = backend
		
		backend.oldCode = code
		backend.code = newCode
		backend.modification = modification
		
		val scope = LexerScopeIncremental(backend)
	}
	
	fun discardAndSetCode(newCode: LlangCode) {
		backend.code = newCode
		parseInitially()
	}
}


class Lexer(code: LlangCode) {
	var oldCode: LlangCode = LlangCode.Empty
	
	var code: LlangCode = code
	
	/**
	 * A coerced modification which works by utilizing separator tokens. (see book.md.)
	 */
	var modification: CodeModification = CodeModification(
		oldCodeSpan = CollectionRange(start = 0, end = 0),
		oldTokenSpan = CollectionRange(start = 0, end = 0),
		insertedCode = code
	)
	
	var tokens: List<Token> = emptyList()
}
