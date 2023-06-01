package com.lhwdev.llang.cst

import com.lhwdev.llang.token.CstToken
import com.lhwdev.llang.token.TokenKind


/**
 * Node for tokens with unimportant token kind, such as `Comma`, `Dot` etc.
 */
class CstLeafNode(val token: CstToken) : CstNode {
	companion object : CstNodeKind<CstLeafNode>
}

fun CstParseContext.cstLeaf(tokenKind: TokenKind, length: Int): CstLeafNode {
	beginNode<CstLeafNode>(light = true)?.let { return it }
	endNode(CstLeafNode(code.token(tokenKind, length)))
}
