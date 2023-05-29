package com.lhwdev.llang.cst

import com.lhwdev.llang.token.TokenKind


class CstLeafNode(c: CstParseContext, tokenKind: TokenKind) : CstNode {
	companion object : CstNodeKind<CstLeafNode> {
		// TODO: possible performance improvement
		fun factory(tokenKind: TokenKind): CstNodeFactory<CstLeafNode> =
			object : CstNodeFactory<CstLeafNode> {
				override fun create(c: CstParseContext) = CstLeafNode(c, tokenKind)
			}
	}
}


@Suppress("NOTHING_TO_INLINE")
inline fun TokenKind.asFactory(): CstNodeFactory<CstLeafNode> =
	CstLeafNode.factory(this)
