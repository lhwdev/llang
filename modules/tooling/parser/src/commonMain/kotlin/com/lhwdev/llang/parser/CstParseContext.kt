package com.lhwdev.llang.parser

import com.lhwdev.llang.cst.structure.CstNode
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
	enum class NodeKind(val isNode: Boolean = false) {
		/**
		 * Nodes with [NodeKind] other than [LeafNode] should not use [code] directly, though not
		 * validated (TODO: validate).
		 */
		LeafNode(isNode = true),
		
		Node(isNode = true),
		
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
		StructuredNode(isNode = true),
		
		Discardable,
		
		/**
		 * Peek nodes exist to support the cases where using [Discardable] is unaffordable.
		 * All resulting nodes from [Peek] should not be inserted into tree via [insertChildNode].
		 */
		Peek,
		
		/**
		 * Provides auxiliary data. Some special data such as LocalContext can be used here.
		 * Data node can call several times to [provideNodeHintToCurrent], then should include a
		 * single child node.
		 */
		Data,
	}
	
	@RequiresOptIn
	annotation class InternalApi
	
	
	/// Token
	
	/**
	 * Can only be used inside [leafNode] by default.
	 * If needed inside other nodes, use [dangerousCode].
	 */
	val code: CstCodeSource
	
	/**
	 * Writing behavior with [dangerousCode] may cause undefined behavior.
	 */
	@InternalApi
	val dangerousCode: CstCodeSource
	
	
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
	
	@InternalApi
	val alwaysRequireNodeInfo: Boolean
	
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
	): Node?
	
	val lastEndError: Throwable?
	
	/**
	 * In most cases, [CstParseContext] can smartly skip unnecessary nodes, so that executing all
	 * parsing code top-down would pose nearly zero overhead. But, in some nodes such as
	 * `CstExpression`, execution order(dsl) does not match the structure. So reaching out to deeper
	 * nodes without calling parent parser is required. This block provide that feature.
	 */
	fun provideRestartBlock(block: CstParseContext.() -> CstNode)
	
	
	sealed interface NodeHint {
		sealed interface ToFollowing : NodeHint
		sealed interface ToCurrent : NodeHint
		
		sealed interface Data : NodeHint
		
		/**
		 * Signals that parent node can never be parsed without current node.
		 *
		 * Useful for hinting discardable parent node from keyword.
		 */
		data object Vital : ToFollowing
		
		/**
		 * Signal to parent discardable node that, if parsing current node succeeds, parent node
		 * should not be discarded. If parent node is [discard]ed, it is replaced with dummy node,
		 * faking callers of discardable node that parsing was successful.
		 *
		 * Used in keyword to improve performance and to prevent weird parsing in IDE.
		 */
		data object PreventDiscard : ToFollowing
		
		
		class ContextLocal<T>(val key: Key<T>, val value: T) : Data, ToCurrent {
			class Key<T>(private val debugName: String? = null, val defaultValue: () -> T) {
				infix fun provides(value: T): ContextLocal<T> = ContextLocal(this, value)
				
				override fun toString() = debugName ?: super.toString()
			}
		}
	}
	
	/**
	 * Hints current node that this [hint] should be applied.
	 */
	fun provideNodeHintToCurrent(hint: NodeHint.ToCurrent)
	
	/**
	 * Hints following [beginChildNode] call that this [hint] should be applied to that child node.
	 */
	fun provideNodeHintToFollowing(hint: NodeHint.ToFollowing)
	
	
	/// Detached mode
	/// - About detached mode, which is an alternative way than DSL-like scope based method to
	///   parse things. If available, detached mode should be avoided.
	//  - NOTE: all detached nodes, that are not attached on proper depth, are bound to the
	//    parent of itself at proper order.
	
	/**
	 * Informs that all child nodes are 'detached'.
	 * Generally raw node tree is determined by the order of invocation of `node {}`. However,
	 * calling this in structured manner is sometimes impossible.
	 *
	 * Detached mode enables separating 'where it is parsed' and 'where it is attached'. Generally
	 * all nodes are implicitly attached to where `node {}` was called. But in detached mode, you
	 * can insert tree node associated with returned node into tree via [insertChildNode].
	 *
	 * This should be called as soon as you call [beginChildNode]. (generally, called first inside
	 * `node {}`.)
	 * It is caller's responsibility that all llang features like discard, incremental parsing,
	 * caching etc. would happen efficiently.
	 *
	 * All child nodes which may contain detached node as child should call this separately.
	 *
	 * ### Whitespace handling
	 * When you call [markCurrentAsDetached], all implicit nodes, including whitespaces, become
	 * attached to parent, which may not be desirable. Using this, implicit node handling is
	 * handled by context.
	 */
	fun markChildrenAsDetached(peek: Boolean = false)
	
	fun endChildrenAsDetached()
	
	
	/**
	 * Similar to [markChildrenAsDetached], but only to current node.
	 *
	 * This can be called anywhere inside node (of course you should match node depth).
	 */
	fun markCurrentAsDetached()
	
	fun <Node : CstNode> insertChildNode(node: Node): Node
	
	fun <Node : CstNode> acceptChildNode(node: Node): Node
	
	
	fun hiddenDebugCommands(command: String, vararg args: Any?): Any? {
		return null
	}
}


typealias CstParseContextLocal<T> = CstParseContext.NodeHint.ContextLocal<T>


object CstParseContextLocals
