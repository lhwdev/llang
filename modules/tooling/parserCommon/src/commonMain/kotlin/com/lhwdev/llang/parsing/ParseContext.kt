package com.lhwdev.llang.parsing

import com.lhwdev.llang.diagnostic.DiagnosticCollector
import com.lhwdev.llang.parsing.util.DiscardException
import com.lhwdev.llang.parsing.util.ParseException

interface ParseContext : DiagnosticCollector {
	fun discard(exception: DiscardException = DiscardException): Nothing
	
	fun parseError(exception: ParseException): Nothing
}


fun ParseContext.parseError(message: String): Nothing {
	parseError(ParseException(message))
}

inline fun ParseContext.parseRequire(condition: Boolean, message: () -> String) {
	if(!condition) parseError(message())
}
