package com.lhwdev.llang.test.lexer

import java.lang.StackWalker.StackFrame


val debugTraceStack = true


private val csi = "\u001B["
private fun csi(c: String) = "${csi}${c}m"

private fun csiItem(code: String): (String) -> String = { "${csi(code)}${it}${csi("0")}" }

object Color {
	val reset = csiItem("0")
	val bold = csiItem("1")
	val italic = csiItem("3")
	val underlined = csiItem("4")
	val red = csiItem("31")
	val lightRed = csiItem("91")
	val lightMagenta = csiItem("95")
	val blue = csiItem("34")
	val yellow = csiItem("33")
	val green = csiItem("32")
	val lightGreen = csiItem("92")
	val cyan = csiItem("96")
	val dimCyan = csiItem("36")
	val gray = csiItem("90")
	val lightGray = csiItem("37")
}

fun escape(code: String) = "\"${code.replace("\n", "\\n")}\""

fun eraseLines(n: Int) {
	if(n <= 0) return
	print("${csi}2K\r")
	repeat(n - 1) { print("${csi}1A${csi}2K") }
}


class StackLocation(val trace: List<StackFrame>) {
	override fun toString(): String =
		trace.take(5)
			.joinToString(separator = "<") { "${it.declaringClass.simpleName}.${it.methodName}" }
	
	fun stackTrace(): String = trace.joinToString(separator = "\n") { it.toString() }
	
	companion object {
		val Stub = StackLocation(trace = emptyList())
	}
}

fun stackLocation(n: Int = 1, count: Int = 10): StackLocation = if(debugTraceStack) {
	val frames = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk {
		it.skip((n + 1).toLong())
			.filter(::stackFramePredicate)
			.limit(count.toLong())
			.toList()
	}
	StackLocation(frames)
} else {
	StackLocation(emptyList())
}

private fun stackFramePredicate(frame: StackFrame): Boolean =
	'$' !in frame.methodName


fun StackLocation.meaningfulStackName(): String =
	meaningfulStackName(or = null)

// do not use default argument: `or: String? = null` as it messes up the stacktrace
fun StackLocation.meaningfulStackName(or: String?): String {
	if(!debugTraceStack) return "(?)"
	
	val element = trace.firstOrNull() ?: return "-"
	
	// simplified heuristic for detecting lambda
	if("$" in element.className && element.methodName == "invoke") {
		if(or != null) return or
		return "(${element.className} lambda)"
	}
	return element.methodName
}
