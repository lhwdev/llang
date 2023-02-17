package com.lhwdev.utils.delegate

import kotlin.reflect.KProperty


@JvmInline
value class StubProperty<T>(val value: T) {
	@Suppress("NOTHING_TO_INLINE")
	inline operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}
