package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.core.CstWhitespace
import com.lhwdev.llang.cst.core.CstWs
import com.lhwdev.llang.parser.*
import com.lhwdev.llang.tokenizer.parseWhitespace


val cstWs = CstNodeFactory { cstWs() }

val cstWhitespace = CstNodeFactory { cstWhitespace() }


fun CstParseContext.cstWs(): CstWs = node(CstWs) {
	0
}


fun CstParseContext.cstWhitespace(): CstWhitespace =
	structuredNode(CstWhitespace) { CstWhitespace(code.parseWhitespace() ?: discard()) }

fun CstParseContext.cstWhitespaceOrNull(): CstWhitespace? =
	nullableStructuredNode(CstWhitespace) { code.parseWhitespace()?.let { CstWhitespace(it) } }
