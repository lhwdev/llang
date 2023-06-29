package com.lhwdev.llang.parser

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstLeafNode

@OptIn(CstParseContext.InternalApi::class)
inline fun <Node : CstNode> CstParseContext.rawNullableNode(
	kind: CstParseContext.NodeKind,
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node?,
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
			endChildNodeWithError(context, throwable = null, info = null)
		}
	} catch(throwable: Throwable) {
		endChildNodeWithError(context, throwable, info) ?: throw throwable
	}
}

inline fun <Node : CstNode> CstParseContext.nullableNode(
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node?,
): Node? = rawNullableNode(CstParseContext.NodeKind.Node, info, block)

inline fun <Node : CstNode> CstParseContext.nullableStructuredNode(
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node?,
): Node? = rawNullableNode(CstParseContext.NodeKind.StructuredNode, info, block)

inline fun <Node : CstLeafNode> CstParseContext.nullableLeafNode(
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node?,
): Node? = rawNullableNode(CstParseContext.NodeKind.LeafNode, info, block)


// `code.acceptToken` is a good fit for this!
inline fun <Node : CstNode> rawRestartableNode(
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
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node,
): Node = rawRestartableNode({ structuredNode(info, it) }, block)

inline fun <Node : CstNode> CstParseContext.restartableNode(
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node,
): Node = rawRestartableNode({ node(info, it) }, block)

inline fun <Node : CstLeafNode> CstParseContext.restartableLeafNode(
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node,
): Node = rawRestartableNode({ leafNode(info, it) }, block)
