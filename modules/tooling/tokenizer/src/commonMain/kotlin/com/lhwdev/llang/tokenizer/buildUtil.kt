package com.lhwdev.llang.tokenizer

import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKind
import com.lhwdev.llang.tokenizer.source.CodeSource


fun CodeSource.requireEmpty() {
	require(currentSpan.isEmpty()) { "non-empty span" }
}

inline fun CodeSource.token(block: CodeSource.() -> TokenKind): Token {
	requireEmpty()
	
	val kind = block()
	return buildToken(kind)
}

inline fun CodeSource.token(kind: TokenKind, block: CodeSource.() -> Unit): Token =
	token {
		block()
		kind
	}
