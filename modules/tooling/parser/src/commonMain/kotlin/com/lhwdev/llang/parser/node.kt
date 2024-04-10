package com.lhwdev.llang.parser

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.core.CstLeafNode
import com.lhwdev.llang.parsing.util.DiscardException


@OptIn(CstParseContext.InternalApi::class)
inline fun <Node : CstNode> CstParseContext.rawNode(
	kind: CstParseContext.NodeKind,
	block: CstParseContext.() -> Node,
): Node {
	val context = beginChildNode(kind) ?: return skipChildNode()
	val nodeGroupId = context.currentNodeGroupId
	return try {
		val node = try {
			context.block()
		} finally {
			context.beforeEndNodeDebugHint(nodeGroupId)
		}
		endChildNode(context, node)
	} catch(throwable: Throwable) {
		endChildNodeWithError(context, throwable) ?: throw throwable
	}
}

@OptIn(CstParseContext.InternalApi::class)
inline fun <Node : CstNode> CstParseContext.discardable(
	block: CstParseContext.() -> Node,
): Node? {
	val context =
		beginChildNode(CstParseContext.NodeKind.Discardable) ?: return skipChildNode()
	val nodeGroupId = context.currentNodeGroupId
	return try {
		val node = try {
			context.block()
		} finally {
			context.beforeEndNodeDebugHint(nodeGroupId)
		}
		endChildNode(context, node)
	} catch(exception: DiscardException) {
		endChildNodeWithError(context, exception)
	} catch(throwable: Throwable) {
		endChildNodeWithError(context, throwable) ?: throw throwable
	}
}

@OptIn(CstParseContext.InternalApi::class)
inline fun <Node : CstNode> CstParseContext.rawNullableNode(
	kind: CstParseContext.NodeKind,
	crossinline block: CstParseContext.() -> Node?,
	onError: (context: CstParseContext, throwable: Throwable) -> Node?,
): Node? {
	val context = beginChildNode(kind) ?: return skipChildNode()
	val nodeGroupId = context.currentNodeGroupId
	return try {
		val node = try {
			context.block()
		} finally {
			context.beforeEndNodeDebugHint(nodeGroupId)
		}
		if(node != null) {
			endChildNode(context, node)
		} else {
			endChildNodeWithError(context, throwable = null)
		}
	} catch(throwable: Throwable) {
		onError(context, throwable)
	}
	
}


inline fun <Node : CstNode> CstParseContext.node(
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	kind = CstParseContext.NodeKind.Node,
	block = block,
)

inline fun <Node : CstNode> CstParseContext.leafNode(
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	kind = CstParseContext.NodeKind.LeafNode,
	block = block,
)

inline fun <Node : CstNode> CstParseContext.structuredNode(
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	kind = CstParseContext.NodeKind.StructuredNode,
	block = block,
)

inline fun <Node : CstNode> CstParseContext.rawDataNode(
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	kind = CstParseContext.NodeKind.Data,
	block = block,
)

inline fun <Node : CstNode> CstParseContext.nullableNode(
	crossinline block: CstParseContext.() -> Node?,
): Node? = rawNullableNode(
	kind = CstParseContext.NodeKind.Node,
	block = block,
	onError = @OptIn(CstParseContext.InternalApi::class) { context, throwable ->
		endChildNodeWithError(context, throwable) ?: throw throwable
	},
)

inline fun <Node : CstNode> CstParseContext.nullableStructuredNode(
	crossinline block: CstParseContext.() -> Node?,
): Node? = rawNullableNode(
	kind = CstParseContext.NodeKind.StructuredNode,
	block = block,
	onError = @OptIn(CstParseContext.InternalApi::class) { context, throwable ->
		endChildNodeWithError(context, throwable) ?: throw throwable
	},
)

inline fun <Node : CstLeafNode> CstParseContext.nullableLeafNode(
	crossinline block: CstParseContext.() -> Node?,
): Node? = rawNullableNode(
	kind = CstParseContext.NodeKind.LeafNode,
	block = block,
	onError = @OptIn(CstParseContext.InternalApi::class) { context, throwable ->
		endChildNodeWithError(context, throwable) ?: throw throwable
	},
)


// `code.acceptToken` is a good fit for this!
@PublishedApi
internal inline fun <Node : CstNode> rawRestartableNode(
	target: (block: CstParseContext.() -> Node) -> Node,
	crossinline block: CstParseContext.() -> Node,
): Node {
	lateinit var newBlock: CstParseContext.() -> Node
	newBlock = {
		try {
			block()
		} finally {
			provideRestartBlock(newBlock)
		}
	}
	return target(newBlock)
}

inline fun <Node : CstNode> CstParseContext.restartableStructuredNode(
	crossinline block: CstParseContext.() -> Node,
): Node = rawRestartableNode({ structuredNode(it) }, block)

inline fun <Node : CstNode> CstParseContext.restartableNode(
	crossinline block: CstParseContext.() -> Node,
): Node = rawRestartableNode({ node(it) }, block)

inline fun <Node : CstLeafNode> CstParseContext.restartableLeafNode(
	crossinline block: CstParseContext.() -> Node,
): Node = rawRestartableNode({ leafNode(it) }, block)
