package com.lhwdev.utils.platform


expect class CallContextLocal<T>(defaultValue: T) {
	val current: T
	
	fun set(value: T)
}

inline fun <T, R> CallContextLocal<T>.withValue(value: T, block: () -> R): R {
	val previous = current
	set(value)
	return try {
		block()
	} finally {
		set(previous)
	}
}
