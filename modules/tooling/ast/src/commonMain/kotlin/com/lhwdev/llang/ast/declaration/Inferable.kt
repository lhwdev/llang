package com.lhwdev.llang.ast.declaration


sealed class Inferable<out T> {
	class Explicit<T>(val value: T) : Inferable<T>()
	
	object Implicit : Inferable<Nothing>()
}


val Inferable<*>.isExplicit: Boolean
	get() = this !is Inferable.Implicit

inline fun <T> Inferable<T>.getOrElse(block: () -> T): T = when(this) {
	Inferable.Implicit -> block()
	is Inferable.Explicit -> value
}

fun <T> Inferable<T>.getOrNull(): T? = getOrElse { null }
