package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.structure.core.CstLeafNode
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.nullableStructuredNode
import com.lhwdev.llang.parser.structuredNode
import com.lhwdev.llang.token.TokenKind
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.source.parseToken
import com.lhwdev.llang.tokenizer.source.parseTokenOrNull


fun CstParseContext.cstLeafNode(tokenKind: TokenKind, content: String): CstLeafNode =
	structuredNode(null) { CstLeafNode(code.parseToken(tokenKind, content)) }

fun CstParseContext.cstLeafNodeOrNull(tokenKind: TokenKind, content: String): CstLeafNode? =
	nullableStructuredNode(null) {
		code.parseTokenOrNull(tokenKind, content)?.let { CstLeafNode(it) }
	}

fun CstParseContext.cstLeafComma(): CstLeafNode.Comma = structuredNode(null) {
	CstLeafNode.Comma(code.parseToken(TokenKinds.Operation.Other.Comma, ","))
}

fun CstParseContext.cstLeafDot(): CstLeafNode.Dot = structuredNode(null) {
	CstLeafNode.Dot(code.parseToken(TokenKinds.Operation.Access.Dot, "."))
}
