package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.structure.core.CstKeyword
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.leafNode
import com.lhwdev.llang.tokenizer.parseKeyword
import com.lhwdev.llang.tokenizer.parseSoftKeyword


fun CstParseContext.cstKeyword(): CstKeyword {
	val node = leafNode(CstKeyword) { CstKeyword(code.parseKeyword()) }
	preventDiscard()
	return node
}

fun CstParseContext.cstSoftKeyword(): CstKeyword =
	leafNode(CstKeyword) { CstKeyword(code.parseSoftKeyword()) }
