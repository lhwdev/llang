package com.lhwdev.llang.tokenizer

import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.source.*


fun CodeSource.parseStringLiteralBegin(): Token = token {
	advanceMatch('"')
	if(matchesAdvance("\"\"")) {
		TokenKinds.StringLiteral.Raw.Begin
	} else {
		TokenKinds.StringLiteral.Escaped.Begin
	}
}

// begin & end is identical in llang
fun CodeSource.parseStringLiteralEnd(): Token = token {
	advanceMatch('"')
	if(matchesAdvance("\"\"")) {
		TokenKinds.StringLiteral.Raw.End
	} else {
		TokenKinds.StringLiteral.Escaped.End
	}
}


/**
 * Consumer should check if `returnedToken.kind == TokenKinds.StringLiteral.TemplateVariable`
 * / `TemplateExpression`. If so, consumer should parse following tokens in expression parsing mode.
 *
 * Consumer should check if `returnedToken.kind is TokenKinds.StringLiteral.QuoteEnd`.
 */
fun CodeSource.parseInStringLiteral(quote: TokenKinds.StringLiteral.Quote): Token = when(current) {
	'\\' -> when(peek()) {
		'\\', '$', 'n', 'r', 't', 'b', '\'', '"' -> token(
			TokenKinds.StringLiteral.EscapedContent,
			2,
		)
		
		'u' -> token(TokenKinds.StringLiteral.EscapedContent, 6)
		
		else -> token(TokenKinds.StringLiteral.EscapedContent) {
			advance(2)
			pushDiagnostic(TokenizerDiagnostic.IllegalStringEscape("$currentSpan"))
		}
	}
	
	'$' -> if(peek() == '{') {
		token(TokenKinds.StringLiteral.TemplateExpression, 2)
	} else {
		token(TokenKinds.StringLiteral.TemplateVariable, 1)
	}
	
	'"' -> when(quote) {
		TokenKinds.StringLiteral.Escaped -> token(TokenKinds.StringLiteral.Escaped.End, 1)
		TokenKinds.StringLiteral.Raw -> if(matches("\"\"", offset = 1)) {
			token(TokenKinds.StringLiteral.Raw.End, 3)
		} else {
			parseLiteralInStringLiteral()
		}
		
		else -> error("unknown quote $quote")
	}
	
	else -> parseLiteralInStringLiteral()
}

private fun CodeSource.parseLiteralInStringLiteral(): Token =
	token(TokenKinds.StringLiteral.Content) {
		advanceWhile { current !in "\\$\"" }
	}
