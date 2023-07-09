package com.lhwdev.llang.parser

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.parsing.ParseContext


@DslMarker
annotation class CstParseContextMarker


/**
 * [CstParseContext] can be originated from raw code source, serialized CST structure, or
 * existing other [CstParseContext] (for cloning).
 *
 * [CstParseContext] enables DSL-based elegant code parsing. In most cases, your code structure
 * matches 1:1 to actual Cst structure and raw cst node tree.
 *
 * [CstParseContext] is write-only for nodes, read-only for code/tokens for performance in most
 * cases. Note that [CstParseContext] itself can become to be seem immutable. (of course, internal
 * CstRTree is mutated) For performance, [beginChildNode] return itself in most cases, but it can
 * return new instance of [CstParseContext] to implement immutable behavior. This is possible when
 * [code] is only accessed in leaf nodes. ([code] in leaf node has temporal mutability)
 * This may enable parallel parsing in one file. Maybe..? Per-declaration?
 *
 * Note that this is very similar to `Composer` of Jetpack Compose. However I could not use
 * Compose Runtime for this parser project as it lacks:
 * - Exception handling. Compose Runtime insists on being 'general purpose tree management ...', but
 *   it seems to me that it leads toward Compose UI.
 * -
 *
 * TODO: possible parallel parsing? *put codes in ThreadPool, run them, profit!*
 */
@CstParseContextMarker
interface CstParseContext : ParseContext {
	enum class NodeKind {
		/**
		 * Nodes with [NodeKind] other than [LeafNode] should not use [code] directly, though not
		 * validated (TODO: validate).
		 */
		LeafNode,
		
		Node,
		
		/**
		 * Exists only for implementation soundness.
		 * Implementation should support this [NodeKind].
		 */
		VirtualStructuredNode,
		
		/**
		 * This can only be used when kind and order of direct child nodes of the node is identical,
		 * which is, inside [structuredNode], you should not use any branching such as `if` `when`
		 * `for` etc.
		 *
		 * Using [structuredNode] as much as available will enable parser internals to cache nodes.
		 * Caching nodes is a vital component of incremental parsing.
		 */
		StructuredNode,
		
		Discardable,
		
		/**
		 * Provides auxiliary data. Some special data such as LocalContext can be used here.
		 * Data node can call several times to [provideData], then should include a single child
		 * node.
		 */
		Data,
	}
	
	@RequiresOptIn
	annotation class InternalApi
	
	
	/// Token
	
	/**
	 * Can only be used inside [leafNode] by default.
	 * If needed inside other nodes, call [allowUsingCodeSource].
	 */
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
	 * Disables inserting implicit node for current node group.
	 */
	fun disableAdjacentImplicitNode()
	
	/**
	 * If there is one or more child nodes in this node, implicit node such as CstWss is parsed
	 * while [beginChildNode] is being called, to ensure proper spacing between nodes.
	 * If you don't want this behavior, call [disableAdjacentImplicitNode].
	 */
	@InternalApi
	fun beginChildNode(kind: NodeKind): CstParseContext?
	
	@InternalApi
	fun beforeEndNodeDebugHint(nodeGroupId: Long) {
	}
	
	@InternalApi
	fun <Node : CstNode> endChildNode(childContext: CstParseContext, node: Node): Node
	
	// @InternalApi
	// fun <Node : CstNode> deferChildParsing(block: CstParseContext.() -> Node): () -> Node
	
	@InternalApi
	fun <Node : CstNode> skipChildNode(): Node
	
	/**
	 * Returns node if graceful error handling is available and applicable for [Node].
	 * `null` otherwise.
	 */
	@InternalApi
	fun <Node : CstNode> endChildNodeWithError(
		childContext: CstParseContext,
		throwable: Throwable?,
		info: CstNodeInfo<Node>?,
	): Node?
	
	val lastEndError: Throwable?
	
	fun allowUsingCodeSource()
	
	/**
	 * In most cases, [CstParseContext] can smartly skip unnecessary nodes, so that executing all
	 * parsing code top-down would pose nearly zero overhead. But, in some nodes such as
	 * `CstExpression`, execution order(dsl) does not match the structure. So reaching out to deeper
	 * nodes without calling parent parser is required. This block provide that feature.
	 */
	fun provideRestartBlock(block: CstParseContext.() -> CstNode)
	
	sealed interface NodeHint {
		/**
		 * Signals that parent node can never be parsed without current node.
		 *
		 * Useful for hinting discardable parent node from keyword.
		 */
		object Vital : NodeHint
		
		/**
		 * Signal to parent discardable node that, if parsing current node succeeds, parent node
		 * should not be discarded. If parent node is [discard]ed, it is replaced with dummy node,
		 * faking callers of discardable node that parsing was successful.
		 *
		 * Used in keyword to improve performance and to prevent weird parsing in IDE.
		 */
		object PreventDiscard : NodeHint
	}
	
	/**
	 * Hints following [beginChildNode] call that this [hint] should be applied to that child node.
	 */
	fun provideNodeHintBeforeBegin(hint: NodeHint)
	
	/**
	 * Informs that child nodes are 'detached'.
	 * Generally raw node tree is determined by the order of invocation of `node {}`. However,
	 * calling this in structured manner is sometimes impossible. So, this informs that the raw tree
	 * of all direct children node of current node should be evaluated manually.
	 *
	 * This should be called as soon as you call [beginChildNode] (generally, called first inside
	 * `node {}`.)
	 *
	 * All child nodes which may contain detached node as child should call [markNestedContainsDetached].
	 */
	fun markContainsDetached()
	
	fun markNestedContainsDetached()
	
	
	sealed interface ProvidedData {
		class ContextLocal<T>(val key: Key<T>, val value: T) : ProvidedData {
			class Key<T>(private val debugName: String? = null, val defaultValue: () -> T) {
				infix fun provides(value: T): ContextLocal<T> = ContextLocal(this, value)
				
				override fun toString() = debugName ?: super.toString()
			}
		}
	}
	
	fun provideData(data: ProvidedData)
}


typealias CstParseContextLocal<T> = CstParseContext.ProvidedData.ContextLocal<T>


object CstParseContextLocals
