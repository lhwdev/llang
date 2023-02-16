package com.lhwdev.llang.diagnostic


interface DiagnosticContext {
	operator fun <T> get(key: DiagnosticContextKey<T>): T
}

class DiagnosticContextKey<T>
