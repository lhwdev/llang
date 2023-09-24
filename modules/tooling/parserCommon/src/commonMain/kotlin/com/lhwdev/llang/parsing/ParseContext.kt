package com.lhwdev.llang.parsing

import com.lhwdev.llang.diagnostic.DiagnosticCollector
import com.lhwdev.llang.parsing.util.DiscardException
import com.lhwdev.llang.parsing.util.ParseException

interface ParseContext : DiagnosticCollector {
	val debugEnabled: Boolean
	fun debug(line: String)
	
	fun discard(
		exception: DiscardException = if(debugEnabled) DiscardException(writeStackTrace = true) else DiscardException,
	): Nothing {
		throw exception
	}
	
	fun parseError(exception: ParseException): Nothing {
		throw exception
	}
}

inline fun ParseContext.debug(block: () -> String) {
	if(debugEnabled) debug(block())
}

inline fun ParseContext.discard(getMessage: () -> String): Nothing {
	if(debugEnabled) {
		discard(DiscardException(message = getMessage(), writeStackTrace = true))
	} else {
		discard()
	}
}


fun ParseContext.parseError(message: String): Nothing {
	parseError(ParseException(message))
}

inline fun ParseContext.parseRequire(condition: Boolean, message: () -> String) {
	if(!condition) parseError(message())
}
