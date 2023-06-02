package com.lhwdev.llang.cst

import com.lhwdev.llang.cst.util.DiscardException
import com.lhwdev.llang.token.CstToken
import com.lhwdev.llang.token.TokenKind


// Possible context kind: declaration / statements / expression
// Possible location: global, class, function, valInit


/**
 * [CstParseContext] can be originated from raw code source, serialized CST structure, or
 * existing other [CstParseContext] (for cloning).
 */
interface CstParseContext {
	/// Token / Cst Output
	val code: CstMutableCodeSource
	
	fun token(kind: TokenKind): CstToken
	
	
	/// Node
	
	fun <Node : CstNode> beginNode(allowWs: Boolean = true, light: Boolean = false): Node?
	
	fun <T> pushNodeLocalContext(key: CstLocalContextKey<T>, value: T)
	
	fun <T> getLocalContext(key: CstLocalContextKey<T>): T
	
	fun <Node : CstNode> beginDiscardable(): Node?
	
	
	
	fun resetCurrentDiscardable()
	
	fun <Node : CstNode> endNode(node: Node): Node
	
	fun discardDiscardable()
}

inline fun <Node : CstNode> CstParseContext.node(
	allowWs: Boolean = true,
	crossinline block: CstNodeFactory<Node>
): Node {
	beginNode<Node>()?.let { return it }
	return endNode(this.block())
}

/**
 * Basic primitive for implementing [CstList][com.lhwdev.llang.cst.util.CstList],
 * [CstSelect][com.lhwdev.llang.cst.common.util.CstSelect] etc.
 * Useful for branching such as: 'possible patterns: [A, B] or [C, D, E]'.
 */
inline fun <Node : CstNode> CstParseContext.discardable(
	crossinline block: CstNodeFactory<Node>
): Node? {
	beginDiscardable<Node>()?.let { return it }
	return try {
		endNode(this.block())
	} catch(e: DiscardException) {
		discardDiscardable()
		null
	}
}
