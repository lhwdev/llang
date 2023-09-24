package com.lhwdev.llang.test.lexer


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

fun meaningfulStackName(n: Int): String =
	meaningfulStackName(n + 1, or = null)

// do not use default argument: `or: String? = null` as it messes up the stacktrace
fun meaningfulStackName(n: Int, or: String?): String {
	if(!debugTraceStack) return "(?)"
	
	val element = Throwable().stackTrace
		.drop(n + 1)
		.first()
	
	// simplified heuristic for detecting lambda
	if("$" in element.className && element.methodName == "invoke") {
		if(or != null) return or
		return "(lambda)"
	}
	return element.methodName
}
