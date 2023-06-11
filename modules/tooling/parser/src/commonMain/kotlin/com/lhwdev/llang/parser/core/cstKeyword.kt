package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.core.CstKeyword
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.structuredNode
import com.lhwdev.llang.tokenizer.parseKeyword
import com.lhwdev.llang.tokenizer.parseSoftKeyword


fun CstParseContext.cstKeyword(): CstKeyword {
	val node = structuredNode(CstKeyword) { CstKeyword(code.parseKeyword()) }
	preventDiscard()
	return node
}

fun CstParseContext.cstSoftKeyword(): CstKeyword =
	structuredNode(CstKeyword) { CstKeyword(code.parseSoftKeyword()) }
