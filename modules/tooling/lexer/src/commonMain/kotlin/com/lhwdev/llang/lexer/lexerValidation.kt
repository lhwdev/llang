package com.lhwdev.llang.lexer

import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenGroup


val TokenGroup.separateStart: Boolean
	get() = when(this) {
		TokenGroup.Word -> true
		TokenGroup.WordOpen -> true
		else -> false
	}

val TokenGroup.separateEnd: Boolean
	get() = when(this) {
		TokenGroup.Word -> true
		TokenGroup.WordClose -> true
		else -> false
	}


fun LexerScope.validateTokens(tokens: List<Token>) {
	if(tokens.size < 2) return
	
	fun onCheck(isError: Boolean, index: Int) {
		if(isError) pushDiagnostic(
			LexerDiagnostic.TokenValidationFailed("adjacent words"),
			index = LexerIndex.Token(index)
		)
	}
	
	for(index in 0 until tokens.size - 1) {
		onCheck(isError = tokens[index].kind.group.separateEnd && tokens[index + 1].kind.group.separateStart, index)
	}
}
