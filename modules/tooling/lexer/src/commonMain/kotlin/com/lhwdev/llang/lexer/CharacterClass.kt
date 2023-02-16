package com.lhwdev.llang.lexer


// Character class, used for first char of token matching

enum class CharacterClass {
	word, // -> identifier, keyword
	number, // -> number
	newline, // -> eol
	whitespace, // -> whitespace
	other; // -> operator, separator, group, comment, string
	
	companion object {
		fun isNumber(char: Char): Boolean = char in '0'..'9'
		
		fun isWord(char: Char): Boolean = char == '_' || char.isLetter()
		
		fun isMiddleWord(char: Char): Boolean = isNumber(char) || isWord(char)
		
		fun isWhitespace(char: Char): Boolean = char.isWhitespace() && !isLineBreak(char)
		
		fun isLineBreak(char: Char): Boolean = char == '\n' || char == '\r'
	}
}


// TODO: unicode surrogate (32-bit) support
fun CharacterClass(from: Char): CharacterClass = when {
	CharacterClass.isNumber(from) -> CharacterClass.number
	CharacterClass.isLineBreak(from) -> CharacterClass.newline
	from.isLetter() -> CharacterClass.word // unicode letter
	from.isWhitespace() -> CharacterClass.whitespace // .isWhitespace() includes \n \r -> early return above
	else -> CharacterClass.other
}
