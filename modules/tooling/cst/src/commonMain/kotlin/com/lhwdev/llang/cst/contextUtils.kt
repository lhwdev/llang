package com.lhwdev.llang.cst

import com.lhwdev.llang.cst.util.DiscardException
import com.lhwdev.llang.token.TokenKind


fun CstParseContext.discard(exception: DiscardException = DiscardException): Nothing {
	throw exception
}

fun CstParseContext.node(tokenKind: TokenKind): CstLeafNode {
	return node { CstLeafNode(token(tokenKind)) }
}

inline fun <Node : CstNode> CstParseContext.nodeOrNull(
	crossinline block: CstNodeFactory<Node>
): Node? = discardable(block)

fun <Node : CstNode> CstParseContext.oneOf(
	vararg blocks: CstNodeFactory<Node>
): Node = node {
	for(block in blocks) {
		try {
			return@node block()
		} catch(e: DiscardException) {
			resetCurrentDiscardable()
		}
	}
	
	discard()
}
