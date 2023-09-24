package com.lhwdev.llang.diagnostic


interface Diagnostic {
	val name: String
	val level: DiagnosticLevel
	
	context(DiagnosticContext)
	fun getMessage(): String
}


fun diagnostic(name: String, level: DiagnosticLevel, message: String): Diagnostic =
	object : Diagnostic {
		override val name: String = name
		override val level: DiagnosticLevel = level
		
		context(DiagnosticContext)
		override fun getMessage(): String = message
	}
