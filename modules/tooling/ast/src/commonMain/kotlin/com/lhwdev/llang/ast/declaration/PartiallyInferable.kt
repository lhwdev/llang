package com.lhwdev.llang.ast.declaration


/**
 * Explicit or Implicit
 */
sealed interface Inferable<out T> : PartiallyInferable<T> {
	class Explicit<T>(val value: T) : PartiallyInferable<T>, Inferable<T>
	class Partial<T>(val value: T) : PartiallyInferable<T>
	object Implicit : com.lhwdev.llang.ast.declaration.Implicit<Nothing>, PartiallyInferable<Nothing>,
		Inferable<Nothing>
}

sealed interface PartiallyInferable<out T>

/**
 * just Implicit, but to add more type
 */
sealed interface Implicit<out T> : PartiallyInferable<T>


val PartiallyInferable<*>.isExplicit: Boolean
	get() = this is Inferable.Explicit

inline fun <T> PartiallyInferable<T>.getOrElse(block: () -> T): T = when(this) {
	is Inferable.Explicit -> value
	else -> block()
}

fun <T> PartiallyInferable<T>.getOrNull(): T? = getOrElse { null }
