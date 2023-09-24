package com.lhwdev.llang.parsing.util


open class LightException(message: String?, writeStackTrace: Boolean = false) :
	RuntimeException(message, null, false, writeStackTrace) {
	// /**
	//  * Overridden for performance
	//  */
	// override fun fillInStackTrace(): Throwable {
	// 	return this
	// }
}

open class DiscardException(message: String? = null, writeStackTrace: Boolean = false) :
	LightException(message, writeStackTrace) {
	companion object : DiscardException(message = null) {
		override fun toString(): String = "DiscardException"
	}
}

class NotMatchedException(message: String = "not matched") : DiscardException(message) {
	companion object {
		val KeywordEncountered =
			NotMatchedException("keyword encountered in local context; it means local declaration is expected.")
	}
}

open class ParseException(message: String?) : LightException(message, writeStackTrace = true)


