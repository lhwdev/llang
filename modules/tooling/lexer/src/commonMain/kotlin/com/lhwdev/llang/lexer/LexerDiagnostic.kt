package com.lhwdev.llang.lexer

import com.lhwdev.llang.diagnostic.DiagnosticLevel
import com.lhwdev.llang.diagnostic.diagnostic


object LexerDiagnostic {
	fun IllegalNumber(message: String) =
		diagnostic("IllegalNumber", DiagnosticLevel.Error, message = "Illegal number token: $message")
	
	fun IllegalStringEscape(value: String) =
		diagnostic("IllegalStringEscape", DiagnosticLevel.Error, message = "Illegal string escape '$value'")
}
