package com.lhwdev.llang.parser

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.nodeInfoOf


@OptIn(CstParseContext.InternalApi::class)
inline fun <Node : CstNode> CstParseContext.rawNode(
	kind: CstParseContext.NodeKind,
	getInfo: () -> CstNodeInfo<Node>?,
	block: CstParseContext.() -> Node,
): Node {
	val context = beginChildNode(kind) ?: return skipChildNode()
	val nodeGroupId = currentNodeGroupId
	return try {
		val node = try {
			context.block()
		} finally {
			context.beforeEndNodeDebugHint(nodeGroupId)
		}
		endChildNode(context, node)
	} catch(throwable: Throwable) {
		endChildNodeWithError(context, throwable, getInfo()) ?: throw throwable
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
			endChildNodeWithError(context, throwable = null, info = null)
		}
	} catch(throwable: Throwable) {
		onError(context, throwable)
	}
	
}


inline fun <Node : CstNode> CstParseContext.node(
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	kind = CstParseContext.NodeKind.Node,
	getInfo = { info },
	block = block,
)

inline fun <reified Node : CstNode> CstParseContext.node(
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	kind = CstParseContext.NodeKind.Node,
	getInfo = { nodeInfoOf<Node>() },
	block = block,
)

inline fun <Node : CstNode> CstParseContext.structuredNode(
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	kind = CstParseContext.NodeKind.StructuredNode,
	getInfo = { info },
	block = block,
)

inline fun <reified Node : CstNode> CstParseContext.structuredNode(
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	kind = CstParseContext.NodeKind.StructuredNode,
	getInfo = { nodeInfoOf<Node>() },
	block = block,
)
