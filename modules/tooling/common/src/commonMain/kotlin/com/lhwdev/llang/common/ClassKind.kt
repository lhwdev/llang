package com.lhwdev.llang.common


enum class ClassKind {
	Class,
	Object,
	Enum,
	Interface,
}


val ClassKind.isAbstract: Boolean?
	get() = when(this) {
		ClassKind.Class -> null
		ClassKind.Object -> false
		ClassKind.Enum -> false
		ClassKind.Interface -> true
	}
