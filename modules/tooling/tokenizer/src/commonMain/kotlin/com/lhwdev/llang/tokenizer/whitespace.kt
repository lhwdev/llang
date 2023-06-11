package com.lhwdev.llang.tokenizer

import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.source.CodeSource
import com.lhwdev.llang.tokenizer.source.advanceWhile
import com.lhwdev.llang.tokenizer.source.current
import com.lhwdev.llang.tokenizer.source.token


fun CodeSource.parseWhitespace(): Token? =
	if(CharacterKind.isWhitespace(current)) token(TokenKinds.Whitespace) {
		advance()
		advanceWhile { CharacterKind.isWhitespace(current) }
	} else null


fun CodeSource.parseLineBreak(): Token? = if(CharacterKind.isLineBreak(current)) {
	token(TokenKinds.LineBreak, 1)
} else {
	null
}
