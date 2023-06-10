package com.lhwdev.llang.parsing.util


open class LightException(message: String?) : RuntimeException(message, null, false, false) {
	// /**
	//  * Overridden for performance
	//  */
	// override fun fillInStackTrace(): Throwable {
	// 	return this
	// }
}

open class DiscardException(message: String?) : LightException(message) {
	companion object : DiscardException(message = null)
}

class NotMatchedException(message: String = "not matched") : DiscardException(message) {
	companion object {
		val KeywordEncountered =
			NotMatchedException("keyword encountered in local context; it means local declaration is expected.")
	}
}

open class ParseException(message: String?) : LightException(message)


