package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.core.CstKeyword
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.node
import com.lhwdev.llang.tokenizer.parseKeyword
import com.lhwdev.llang.tokenizer.parseSoftKeyword


fun CstParseContext.cstKeyword(): CstKeyword {
	val node = node(CstKeyword) { CstKeyword(code.parseKeyword()) }
	preventDiscard()
	return node
}

fun CstParseContext.cstSoftKeyword(): CstKeyword =
	node(CstKeyword) { CstKeyword(code.parseSoftKeyword()) }
