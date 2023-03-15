package com.lhwdev.llang.common


enum class ClassKind {
	Class,
	Object,
	EnumClass,
	Interface,
}


val ClassKind.isAbstract: Boolean?
	get() = when(this) {
		ClassKind.Class -> null
		ClassKind.Object -> false
		ClassKind.EnumClass -> false
		ClassKind.Interface -> true
	}
