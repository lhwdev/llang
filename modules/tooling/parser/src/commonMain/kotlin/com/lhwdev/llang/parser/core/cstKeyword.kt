package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstKeyword
import com.lhwdev.llang.cst.structure.core.CstLeafNode
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.leafNode
import com.lhwdev.llang.parser.nullableLeafNode
import com.lhwdev.llang.token.TokenKind
import com.lhwdev.llang.tokenizer.parseKeyword
import com.lhwdev.llang.tokenizer.parseSoftKeyword
import com.lhwdev.llang.tokenizer.source.parseToken
import com.lhwdev.llang.tokenizer.source.parseTokenOrNull


inline fun <Node : CstLeafNode> CstParseContext.keywordLeafNode(
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node,
): Node {
	provideNodeHintBeforeBegin(CstParseContext.NodeHint.Vital)
	provideNodeHintBeforeBegin(CstParseContext.NodeHint.PreventDiscard)
	return leafNode(info, block)
}

fun CstParseContext.cstKeyword(): CstKeyword =
	keywordLeafNode(CstKeyword) { CstKeyword(code.parseKeyword()) }

fun CstParseContext.cstKeyword(tokenKind: TokenKind, content: String): CstKeyword =
	keywordLeafNode(CstKeyword) { CstKeyword(code.parseToken(tokenKind, content)) }


fun CstParseContext.cstSoftKeywordOrNull(tokenKind: TokenKind, content: String): CstKeyword? =
	nullableLeafNode(CstKeyword) {
		code.parseTokenOrNull(tokenKind, content)?.let { CstKeyword(it) }
	}

fun CstParseContext.cstSoftKeyword(): CstKeyword =
	leafNode(CstKeyword) { CstKeyword(code.parseSoftKeyword()) }
