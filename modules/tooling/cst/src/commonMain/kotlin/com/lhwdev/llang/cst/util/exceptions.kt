package com.lhwdev.llang.cst.util


open class DiscardException(message: String?) : RuntimeException(message) {
	companion object : DiscardException(message = null)
	
	/**
	 * Overridden for performance
	 */
	override fun fillInStackTrace(): Throwable {
		return this
	}
}

class NotMatchedException(message: String = "not matched") : DiscardException(message) {
	companion object {
		val KeywordEncountered =
			NotMatchedException("keyword encountered in local context; it means local declaration is expected.")
	}
}
