package com.lhwdev.llang.tokenizer

import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.source.*


fun CodeSource.parseNumber(): Token {
	if(current == '0') when(peek()) {
		'x' -> return token(TokenKinds.NumberLiteral.Hex) {
			advance(2)
			@Suppress("SpellCheckingInspection")
			advanceWhile { current in "0123456789abcdef" }
		}
		
		'b' -> return token(TokenKinds.NumberLiteral.Binary) {
			advance(2)
			advanceWhile { current in "01" }
		}
		
		else -> Unit
	}
	
	return token {
		var hasDot = false
		var hasE = false
		
		advanceWhile {
			when(current) {
				in '0'..'9' -> true
				'.' -> {
					hasDot = true
					true
				}
				
				'e' -> {
					if(hasE) {
						pushDiagnostic(TokenizerDiagnostic.IllegalNumber(message = "illegal scientific(e) notation"))
					}
					hasE = true
					true // validation of notation is up to cst parser
				}
				
				else -> false
			}
		}
		
		requireNotEmpty()
		if(hasDot) {
			TokenKinds.NumberLiteral.Float
		} else {
			TokenKinds.NumberLiteral.Integer
		}
	}
}
