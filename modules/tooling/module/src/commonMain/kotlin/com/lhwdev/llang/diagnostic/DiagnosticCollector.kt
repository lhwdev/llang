package com.lhwdev.llang.diagnostic


interface DiagnosticCollector {
	fun pushDiagnostic(diagnostic: Diagnostic)
}
