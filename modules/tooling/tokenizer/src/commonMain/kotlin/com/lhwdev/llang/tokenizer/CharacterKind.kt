package com.lhwdev.llang.tokenizer


object CharacterKind {
	fun isLetter(char: Char): Boolean = char.isLetter()
	
	fun isDigit(char: Char): Boolean = char.isDigit()
	
	
	fun isIdentifier(char: Char): Boolean = isLetter(char) || isDigit(char)
}
