package com.lhwdev.llang.parser

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.CstNodeInfo
import com.lhwdev.llang.parsing.util.ParseContext


// Possible context kind: declaration / statements / expression
// Possible location: global, class, function, valInit


/**
 * [CstParseContext] can be originated from raw code source, serialized CST structure, or
 * existing other [CstParseContext] (for cloning).
 */
interface CstParseContext : CstLocalContextSource, ParseContext {
	@RequiresOptIn
	annotation class InternalApi
	
	
	/// Token
	val code: CstCodeSource
	
	
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
	
	/**
	 * Discardable node is intended to be very light. Nodes returned from discardable is not
	 * cached for incremental parsing. If needed, wrap `discardable {}` with `node {}`.
	 */
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
	
	/**
	 * Signal to parent discardable node that this node is not discardable. Great way to optimize
	 * performance. No-op when parent node itself is not discardable node.
	 *
	 * Use cases: keyword(`class`, `fun`, `val` ...)
	 */
	fun preventDiscard()
}