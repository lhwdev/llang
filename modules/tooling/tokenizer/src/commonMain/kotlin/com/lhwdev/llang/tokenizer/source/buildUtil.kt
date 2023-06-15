package com.lhwdev.llang.tokenizer.source

import com.lhwdev.llang.parsing.util.parseRequire
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKind
import com.lhwdev.llang.token.TokenKinds


fun CodeSource.requireEmpty() {
	parseRequire(currentSpan.isEmpty()) { "non-empty span" }
}

fun CodeSource.requireNotEmpty() {
	parseRequire(currentSpan.isNotEmpty()) { "empty span" }
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

fun CodeSource.token(kind: TokenKind, length: Int = 1): Token = token(kind) {
	advance(length)
}

fun CodeSource.parseToken(kind: TokenKind, content: String): Token = token(kind) {
	advanceMatch(content)
}

fun CodeSource.parseTokenOrNull(kind: TokenKind, content: String): Token? = token(kind) {
	if(!matchesAdvance(content)) return null
}

fun CodeSource.illegalToken(length: Int = 1): Token =
	token(kind = TokenKinds.Illegal, length = length)

inline fun CodeSource.tokenOrNull(block: CodeSource.() -> Token): Token? = try {
	block()
} catch(throwable: Throwable) {
	resetToSpanStart()
	null
}

