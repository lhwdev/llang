package com.lhwdev.llang.diagnostic


interface DiagnosticContext {
	operator fun <T> get(key: DiagnosticContextKey<T>): T
}

class DiagnosticContextKey<T>(val defaultValue: () -> T)


object StubDiagnosticContext : DiagnosticContext {
	override fun <T> get(key: DiagnosticContextKey<T>): T = key.defaultValue()
}
