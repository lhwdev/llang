package com.lhwdev.llang.common


sealed class BodyOmissionKind(val isActual: Boolean) {
	val isExpect: Boolean
		get() = !isActual
	
	abstract class ExpectKind : BodyOmissionKind(isActual = false)
	
	abstract class ActualKind : BodyOmissionKind(isActual = true)
	
	
	object Expect : ExpectKind()
	
	object External : ExpectKind()
	
	object Actual : ExpectKind()
}


val BodyOmissionKind?.isExpect: Boolean
	get() = this?.isExpect ?: false

val BodyOmissionKind?.isActual: Boolean
	get() = this?.isActual ?: false
