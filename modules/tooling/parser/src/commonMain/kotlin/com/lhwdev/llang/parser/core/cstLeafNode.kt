package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.core.CstLeafNode
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.structuredNode
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.source.parseToken


fun CstParseContext.cstLeafComma(): CstLeafNode.Comma = structuredNode(info = null) {
	CstLeafNode.Comma(code.parseToken(TokenKinds.Operation.Other.Comma, ","))
}

fun CstParseContext.cstLeafDot(): CstLeafNode.Dot = structuredNode(info = null) {
	CstLeafNode.Dot(code.parseToken(TokenKinds.Operation.Access.Dot, "."))
}
