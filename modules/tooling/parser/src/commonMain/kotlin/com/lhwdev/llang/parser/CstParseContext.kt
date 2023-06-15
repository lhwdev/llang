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
	enum class NodeKind { Node, StructuredNode, Discardable }
	
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
	
	/**
	 * Disables inserting implicit node for current node group..
	 */
	fun disableAdjacentImplicitNode()
	
	/**
	 * If there is one or more child nodes in this node, implicit node such as CstWss is parsed
	 * while [beginNode] is being called, to ensure proper spacing between nodes.
	 * If you don't want this behavior, call [disableAdjacentImplicitNode].
	 */
	@InternalApi
	fun <Node : CstNode> beginNode(kind: NodeKind): Node?
	
	@InternalApi
	fun beforeEndNodeDebugHint(nodeGroupId: Long) {
	}
	
	
	@InternalApi
	fun <Node : CstNode> endNode(node: Node): Node
	
	/**
	 * Returns node if graceful error handling is available and applicable for [Node].
	 * `null` otherwise.
	 */
	@InternalApi
	fun <Node : CstNode> endNodeWithError(throwable: Throwable?, info: CstNodeInfo<Node>?): Node?
	
	/**
	 * Signal to current discardable node that do not discard itself. Great way to optimize
	 * performance. No-op when current node is not discardable node.
	 *
	 * Use cases: keyword(`class`, `fun`, `val` ...), some operators
	 */
	fun preventDiscard()
}
