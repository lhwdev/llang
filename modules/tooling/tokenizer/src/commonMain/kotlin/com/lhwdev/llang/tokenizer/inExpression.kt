package com.lhwdev.llang.tokenizer

import com.lhwdev.llang.token.Token
import com.lhwdev.llang.tokenizer.source.CodeSource
import com.lhwdev.llang.tokenizer.source.current


fun CodeSource.parseExpressionToken(): Token {
	val c = current
	return when {
		CharacterKind.isLetter(c) -> parseIdentifier()
		CharacterKind.isDigit(c) -> parseNumber()
		// TODO: other cases?
		else -> parseOperationInAnyExpression()
	}
}
