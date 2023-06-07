package com.lhwdev.llang.tokenizer

import com.lhwdev.llang.parsing.util.parseError
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKind
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.source.*
import com.lhwdev.utils.string.isLineBreak


fun CodeSource.parseIdentifier(): Token = token {
	advanceOnIdentifier()
}

fun CodeSource.advanceOnIdentifier(): TokenKind = if(current == '`') {
	advance()
	advanceWhile {
		if(current.isLineBreak()) {
			parseError("no line break inside ` identifier")
		}
		// possible more restrictions by platform
		
		current != '`'
	}
	advanceMatch('`')
	TokenKinds.Identifier.Quoted
} else {
	advanceMatch { CharacterKind.isLetter(current) }
	advanceWhile { CharacterKind.isIdentifier(current) }
	
	TokenKinds.Identifier.Simple
}

