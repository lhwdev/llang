package com.lhwdev.llang.source


open class DiscardException : RuntimeException() {
	/**
	 * Overridden for performance
	 */
	override fun fillInStackTrace(): Throwable {
		return this
	}
}

class NotMatchedException : DiscardException() {
	override val message get() = "not matched"
}
