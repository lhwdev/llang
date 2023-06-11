package com.lhwdev.llang.tokenizer

import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.source.*


fun CodeSource.parseCommentBeginOrNull(): Token? = token {
	if(!matchesAdvance('/')) return null
	
	when(current) {
		'/' -> {
			advance()
			TokenKinds.Comment.Eol.Begin
		}
		
		'*' -> {
			advance()
			if(current == '*') {
				TokenKinds.Comment.LDocBlock.Begin
			} else {
				TokenKinds.Comment.Block.Begin
			}
		}
		
		else -> return null
	}
}

fun CodeSource.parseInEolComment(): Token =
	token(TokenKinds.Comment.Content) { advanceToAheadEol() }

fun CodeSource.parseInBlockComment(kind: TokenKinds.Comment.BlockKind): Token = when(current) {
	'/' -> when(peek()) {
		'*' -> token(TokenKinds.Comment.Block.Begin, 2)
		else -> parseLiteralInBlockComment()
	}
	
	'*' -> when(peek()) {
		'/' -> token(kind.End, 2)
		else -> parseLiteralInBlockComment()
	}
	
	else -> parseLiteralInBlockComment()
}

private fun CodeSource.parseLiteralInBlockComment(): Token = token(TokenKinds.Comment.Content) {
	advanceWhile { current !in "/*" }
}
