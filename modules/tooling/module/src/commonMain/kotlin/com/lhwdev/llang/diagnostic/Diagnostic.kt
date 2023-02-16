package com.lhwdev.llang.diagnostic


interface Diagnostic {
	val name: String
	
	context(DiagnosticContext)
	fun getMessage(): String
}
