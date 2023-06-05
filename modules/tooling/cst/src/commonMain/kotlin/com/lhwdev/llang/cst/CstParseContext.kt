package com.lhwdev.llang.cst

import com.lhwdev.llang.parsing.util.ParseContext
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKind


// Possible context kind: declaration / statements / expression
// Possible location: global, class, function, valInit


/**
 * [CstParseContext] can be originated from raw code source, serialized CST structure, or
 * existing other [CstParseContext] (for cloning).
 */
interface CstParseContext : CstLocalContextSource, ParseContext {
	@RequiresOptIn
	annotation class InternalApi
	
	
	/// Token / Cst Output
	val code: CstCodeSource
	
	fun token(kind: TokenKind): Token
	
	
	// Local Context
	
	fun <T> pushNodeLocalContext(key: CstLocalContextKey<T>, value: T)
	
	override fun <T> getLocalContext(key: CstLocalContextKey<T>): T
	
	
	/// Node
	
	/**
	 * **Not guaranteed to be different** cross different node group.
	 * Can be used to debug behavior.
	 */
	@InternalApi
	val currentNodeGroupId: Long
		get() = 0
	
	@InternalApi
	fun <Node : CstNode> beginNode(): Node?
	
	@InternalApi
	fun <Node : CstNode> beginDiscardableNode(): Node?
	
	@InternalApi
	fun beforeEndNodeDebugHint(nodeGroupId: Long) {
	}
	
	
	@InternalApi
	fun <Node : CstNode> endNode(node: Node): Node
	
	@InternalApi
	fun <Node : CstNode> endDiscardableNode(node: Node): Node
	
	/**
	 * Returns node if graceful error handling is available and applicable for [Node].
	 * `null` otherwise.
	 */
	@InternalApi
	fun <Node : CstNode> endNodeWithError(throwable: Throwable, info: CstNodeInfo<Node>?): Node?
	
	/**
	 * Returns node if graceful error handling is available and applicable for [Node].
	 * `null` otherwise.
	 */
	@InternalApi
	fun <Node : CstNode> endDiscardableNodeWithError(
		throwable: Throwable,
		info: CstNodeInfo<Node>?
	): Node?
	
	
	fun resetCurrentDiscardableNode()
}

@OptIn(CstParseContext.InternalApi::class)
inline fun <reified Node : CstNode> CstParseContext.node(
	crossinline block: CstNodeFactory<Node>
): Node {
	beginNode<Node>()?.let { return it }
	val nodeGroupId = currentNodeGroupId
	return try {
		val node = try {
			this.block()
		} finally {
			beforeEndNodeDebugHint(nodeGroupId)
		}
		endNode(node)
	} catch(throwable: Throwable) {
		endNodeWithError(throwable, nodeInfoOf<Node>()) ?: throw throwable
	}
}

/**
 * Basic primitive for implementing [CstList][com.lhwdev.llang.cst.util.CstList],
 * [CstSelect][com.lhwdev.llang.cst.common.util.CstSelect] etc.
 * Useful for branching such as: 'possible patterns: [A, B] or [C, D, E]'.
 */
@OptIn(CstParseContext.InternalApi::class)
inline fun <reified Node : CstNode> CstParseContext.discardable(
	crossinline block: CstNodeFactory<Node>
): Node? {
	beginDiscardableNode<Node>()?.let { return it }
	val nodeGroupId = currentNodeGroupId
	return try {
		val node = try {
			this.block()
		} finally {
			beforeEndNodeDebugHint(nodeGroupId)
		}
		endDiscardableNode(node)
	} catch(throwable: Throwable) {
		endDiscardableNodeWithError(throwable, nodeInfoOf<Node>())
	}
}
