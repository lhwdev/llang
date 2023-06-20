package com.lhwdev.llang.parser

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.parsing.util.ParseContext


// Possible context kind: declaration / statements / expression
// Possible location: global, class, function, valInit


/**
 * [CstParseContext] can be originated from raw code source, serialized CST structure, or
 * existing other [CstParseContext] (for cloning).
 *
 * [CstParseContext] enables DSL-based elegant code parsing. In most cases, your code structure
 * matches 1:1 to actual Cst structure and raw cst node tree.
 *
 * [CstParseContext] is write-only for nodes, read-only for code/tokens for performance.
 *
 * TODO: possible parallel parsing? *put codes in ThreadPool, run them, profit!*
 */
interface CstParseContext : ParseContext {
	enum class NodeKind {
		/**
		 * Nodes with [NodeKind] other than [LeafNode] should not use [code] directly, though not
		 * validated (TODO: validate).
		 */
		LeafNode,
		
		Node,
		
		/**
		 * This can only be used when kind and order of direct child nodes of the node is identical,
		 * which is, inside [structuredNode], you should not use any branching such as `if` `when`
		 * `for` etc.
		 *
		 * Using [StructuredNode] as much as available will enable parser internals to cache nodes.
		 * Caching nodes is a vital component of incremental parsing.
		 */
		StructuredNode,
		
		Discardable
	}
	
	@RequiresOptIn
	annotation class InternalApi
	
	
	/// Token
	val code: CstCodeSource
	
	
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
	
	/**
	 * Informs that children nodes are 'detached'.
	 * Generally raw node tree is determined by the order of invocation of `node {}`. However,
	 * calling this in structured manner is sometimes impossible. So, this informs that the raw tree
	 * of all direct children node of current node should be evaluated manually.
	 *
	 * This should be called as soon as you call [beginNode] (generally, called first inside
	 * `node {}`.)
	 */
	fun detachedChildrenNode()
}
