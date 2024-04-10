package com.lhwdev.utils.platform

actual class CallContextLocal<T> actual constructor(defaultValue: T) {
	private val threadLocal = object : ThreadLocal<T>() {
		override fun initialValue(): T = defaultValue
	}
	
	actual val current: T
		get() = threadLocal.get()
	
	actual fun set(value: T) {
		threadLocal.set(value)
	}
}
