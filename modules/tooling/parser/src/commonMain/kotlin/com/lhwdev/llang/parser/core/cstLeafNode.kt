package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.structure.core.CstLeafNode
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.leafNode
import com.lhwdev.llang.parser.nullableLeafNode
import com.lhwdev.llang.token.TokenKind
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.source.parseToken
import com.lhwdev.llang.tokenizer.source.parseTokenOrNull


fun CstParseContext.cstLeafNode(tokenKind: TokenKind, content: String): CstLeafNode =
	leafNode(null) { CstLeafNode(code.parseToken(tokenKind, content)) }

fun CstParseContext.cstLeafNodeOrNull(tokenKind: TokenKind, content: String): CstLeafNode? =
	nullableLeafNode(null) {
		code.parseTokenOrNull(tokenKind, content)?.let { CstLeafNode(it) }
	}

fun CstParseContext.cstLeafCommaOrNull(): CstLeafNode.Comma? = nullableLeafNode(null) {
	code.parseTokenOrNull(TokenKinds.Operator.Other.Comma, ",")?.let { CstLeafNode.Comma(it) }
}

fun CstParseContext.cstLeafDot(): CstLeafNode.Dot = leafNode(null) {
	CstLeafNode.Dot(code.parseToken(TokenKinds.Operator.Access.Dot, "."))
}
