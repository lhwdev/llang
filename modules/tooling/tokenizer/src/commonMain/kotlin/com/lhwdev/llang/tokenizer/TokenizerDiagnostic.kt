package com.lhwdev.llang.tokenizer

import com.lhwdev.llang.diagnostic.DiagnosticLevel
import com.lhwdev.llang.diagnostic.diagnostic


object TokenizerDiagnostic {
	fun IllegalNumber(message: String) =
		diagnostic(
			"IllegalNumber",
			DiagnosticLevel.Error,
			message = "Illegal number token: $message"
		)
	
	fun IllegalStringEscape(value: String) =
		diagnostic(
			"IllegalStringEscape",
			DiagnosticLevel.Error,
			message = "Illegal string escape '$value'"
		)
	
	fun TokenValidationFailed(message: String) =
		diagnostic(
			"TokenValidationFailed",
			DiagnosticLevel.Error,
			message = "Token validation failed: $message"
		)
}
