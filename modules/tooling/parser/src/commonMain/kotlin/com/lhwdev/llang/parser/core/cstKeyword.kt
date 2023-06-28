package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.structure.core.CstKeyword
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.leafNode
import com.lhwdev.llang.parser.nullableLeafNode
import com.lhwdev.llang.token.TokenKind
import com.lhwdev.llang.tokenizer.parseKeyword
import com.lhwdev.llang.tokenizer.parseSoftKeyword
import com.lhwdev.llang.tokenizer.source.parseTokenOrNull


fun CstParseContext.cstKeyword(): CstKeyword {
	val node = leafNode(CstKeyword) { CstKeyword(code.parseKeyword()) }
	preventDiscard()
	return node
}


fun CstParseContext.cstSoftKeywordOrNull(tokenKind: TokenKind, content: String): CstKeyword? =
	nullableLeafNode(CstKeyword) {
		code.parseTokenOrNull(tokenKind, content)?.let { CstKeyword(it) }
	}

fun CstParseContext.cstSoftKeyword(): CstKeyword =
	leafNode(CstKeyword) { CstKeyword(code.parseSoftKeyword()) }
