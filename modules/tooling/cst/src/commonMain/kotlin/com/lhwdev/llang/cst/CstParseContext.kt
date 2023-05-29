package com.lhwdev.llang.cst

import com.lhwdev.llang.source.CodeSource
import com.lhwdev.llang.source.DiscardException
import com.lhwdev.llang.token.CstToken
import com.lhwdev.llang.token.TokenKind


/**
 * [CstParseContext] can be originated from raw code source, serialized CST structure, or
 * existing other [CstParseContext] (for cloning).
 */
interface CstParseContext : CodeSource {
	/// Token / Cst Output
	
	fun token(kind: TokenKind): CstToken
	
	
	/// Tree
	
	fun <T : CstNode> beginTree(kind: CstNodeKind<T>): T?
	
	fun beginDiscardableTree()
	
	fun <T : CstNode> endTree(node: T): T
	
	fun discardTree()
}

inline fun <T : CstNode> CstParseContext.tree(kind: CstNodeKind<T>, block: () -> T): T {
	beginTree(kind)?.let { return it }
	return endTree(block())
}

/**
 * Basic primitive for implementing [CstList][com.lhwdev.llang.cst.common.CstList],
 * [CstSelect][com.lhwdev.llang.cst.common.CstSelect] etc.
 * Useful for branching such as: 'possible patterns: [A, B] or [C, D, E]'.
 */
inline fun <T : CstNode> CstParseContext.discardableTree(kind: CstNodeKind<T>, block: () -> T): T? {
	beginTree(kind)?.let { return it }
	return try {
		endTree(block())
	} catch(e: DiscardException) {
		discardTree()
		null
	}
}
