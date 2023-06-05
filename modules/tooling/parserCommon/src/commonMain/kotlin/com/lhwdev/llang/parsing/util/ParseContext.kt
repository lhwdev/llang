package com.lhwdev.llang.parsing.util

interface ParseContext {
	fun discard(exception: DiscardException = DiscardException): Nothing
	
	fun parseError(exception: ParseException): Nothing
}


fun ParseContext.parseError(message: String): Nothing {
	parseError(ParseException(message))
}
