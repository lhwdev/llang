package com.lhwdev.llang.tokenizer


object CharacterKind {
	fun isLetter(char: Char): Boolean = char.isLetter() || char == '_'
	
	fun isDigit(char: Char): Boolean = char.isDigit()
	
	fun isIdentifier(char: Char): Boolean = isLetter(char) || isDigit(char)
	
	fun isWhitespace(char: Char): Boolean = char.isWhitespace()
	
	fun isLineBreak(char: Char): Boolean = char == '\n' || char == '\r'
}
