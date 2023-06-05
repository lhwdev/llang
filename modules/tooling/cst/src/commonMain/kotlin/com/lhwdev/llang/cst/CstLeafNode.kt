package com.lhwdev.llang.cst

import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKind
import com.lhwdev.llang.tokenizer.source.advanceMatches
import com.lhwdev.llang.tokenizer.token


/**
 * Node for tokens with unimportant token kind, such as `Comma`, `Dot` etc.
 */
class CstLeafNode(val token: Token) : CstNode {
	companion object Info : CstNodeInfo<CstLeafNode>
}

fun CstParseContext.cstLeaf(tokenKind: TokenKind, content: String): CstLeafNode = node {
	val token = code.token(tokenKind) { advanceMatches(content) }
	CstLeafNode(token)
}
