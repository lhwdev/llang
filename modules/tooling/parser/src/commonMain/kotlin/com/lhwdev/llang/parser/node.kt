package com.lhwdev.llang.parser

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstLeafNode
import com.lhwdev.llang.parsing.util.DiscardException


@OptIn(CstParseContext.InternalApi::class)
inline fun <Node : CstNode> CstParseContext.rawNode(
	kind: CstParseContext.NodeKind,
	info: CstNodeInfo<Node>? = null,
	block: CstParseContext.() -> Node,
): Node {
	val context = beginChildNode(kind, info = info) ?: return skipChildNode()
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
	info: CstNodeInfo<Node>? = null,
	block: CstParseContext.() -> Node,
): Node? {
	val context =
		beginChildNode(CstParseContext.NodeKind.Discardable, info = info) ?: return skipChildNode()
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
	info: CstNodeInfo<Node>? = null,
	crossinline block: CstParseContext.() -> Node?,
	onError: (context: CstParseContext, throwable: Throwable) -> Node?,
): Node? {
	val context = beginChildNode(kind, info = info) ?: return skipChildNode()
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
	info: CstNodeInfo<Node>? = null,
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	kind = CstParseContext.NodeKind.Node,
	info = info,
	block = block,
)

inline fun <Node : CstNode> CstParseContext.structuredNode(
	info: CstNodeInfo<Node>? = null,
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	kind = CstParseContext.NodeKind.StructuredNode,
	info = info,
	block = block,
)

inline fun <Node : CstNode> CstParseContext.rawDataNode(
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	kind = CstParseContext.NodeKind.Data,
	info = null,
	block = block,
)

inline fun <Node : CstNode> CstParseContext.nullableNode(
	info: CstNodeInfo<Node>? = null,
	crossinline block: CstParseContext.() -> Node?,
): Node? = rawNullableNode(
	kind = CstParseContext.NodeKind.Node,
	info = info,
	block = block,
	onError = @OptIn(CstParseContext.InternalApi::class) { context, throwable ->
		endChildNodeWithError(context, throwable) ?: throw throwable
	},
)

inline fun <Node : CstNode> CstParseContext.nullableStructuredNode(
	info: CstNodeInfo<Node>? = null,
	crossinline block: CstParseContext.() -> Node?,
): Node? = rawNullableNode(
	kind = CstParseContext.NodeKind.StructuredNode,
	info = info,
	block = block,
	onError = @OptIn(CstParseContext.InternalApi::class) { context, throwable ->
		endChildNodeWithError(context, throwable) ?: throw throwable
	},
)

inline fun <Node : CstLeafNode> CstParseContext.nullableLeafNode(
	info: CstNodeInfo<Node>? = null,
	crossinline block: CstParseContext.() -> Node?,
): Node? = rawNullableNode(
	kind = CstParseContext.NodeKind.LeafNode,
	info = info,
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
	info: CstNodeInfo<Node>? = null,
	crossinline block: CstParseContext.() -> Node,
): Node = rawRestartableNode({ structuredNode(info, it) }, block)

inline fun <Node : CstNode> CstParseContext.restartableNode(
	info: CstNodeInfo<Node>? = null,
	crossinline block: CstParseContext.() -> Node,
): Node = rawRestartableNode({ node(info, it) }, block)

inline fun <Node : CstLeafNode> CstParseContext.restartableLeafNode(
	info: CstNodeInfo<Node>? = null,
	crossinline block: CstParseContext.() -> Node,
): Node = rawRestartableNode({ leafNode(info, it) }, block)
