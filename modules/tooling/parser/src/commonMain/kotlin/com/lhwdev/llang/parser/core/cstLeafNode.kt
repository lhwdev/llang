package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.structure.core.CstLeafNode
import com.lhwdev.llang.cst.structure.core.CstLeafNodeImpl
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.leafNode
import com.lhwdev.llang.parser.nullableLeafNode
import com.lhwdev.llang.parser.rawNode
import com.lhwdev.llang.token.TokenKind
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.source.parseToken
import com.lhwdev.llang.tokenizer.source.parseTokenOrNull
import com.lhwdev.llang.tokenizer.source.token


fun CstParseContext.cstPeek(): Char = rawNode(CstParseContext.NodeKind.Peek) {
	leafNode { CstLeafNodeImpl(code.token(TokenKinds.Identifier.Simple)) }
}.token.code.first()

fun CstParseContext.cstLeafNode(tokenKind: TokenKind, content: String): CstLeafNode =
	leafNode { CstLeafNodeImpl(code.parseToken(tokenKind, content)) }

fun CstParseContext.cstLeafNodeOrNull(tokenKind: TokenKind, content: String): CstLeafNode? =
	nullableLeafNode {
		code.parseTokenOrNull(tokenKind, content)?.let { CstLeafNodeImpl(it) }
	}

fun CstParseContext.cstLeafCommaOrNull(): CstLeafNode.Comma? = nullableLeafNode {
	code.parseTokenOrNull(TokenKinds.Operator.Other.Comma, ",")?.let { CstLeafNode.Comma(it) }
}

fun CstParseContext.cstLeafDot(): CstLeafNode.Dot = leafNode {
	CstLeafNode.Dot(code.parseToken(TokenKinds.Operator.Access.Dot, "."))
}

fun CstParseContext.cstLeafColonOrNull(): CstLeafNode.Colon? = nullableLeafNode {
	code.parseTokenOrNull(TokenKinds.Operator.Other.Colon, ":")?.let { CstLeafNode.Colon(it) }
}
