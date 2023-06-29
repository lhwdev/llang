package com.lhwdev.llang.parser

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.nodeInfoOf


@OptIn(CstParseContext.InternalApi::class)
inline fun <Node : CstNode, Return> CstParseContext.rawNode(
	kind: CstParseContext.NodeKind,
	getInfo: () -> CstNodeInfo<Node>?,
	block: CstParseContext.() -> Node,
	onSuccess: (node: Node) -> Return, // workaround for forbidden `Node : CstNode, Node : Return`.
	onError: (throwable: Throwable) -> Return,
): Return {
	val context = beginChildNode(kind) ?: return onSuccess(skipChildNode())
	val nodeGroupId = currentNodeGroupId
	return try {
		val node = try {
			context.block()
		} finally {
			context.beforeEndNodeDebugHint(nodeGroupId)
		}
		onSuccess(endChildNode(context, node))
	} catch(throwable: Throwable) {
		val dummy = endChildNodeWithError(context, throwable, getInfo())
		if(dummy != null) {
			onSuccess(dummy)
		} else {
			onError(throwable)
		}
	}
}


fun <Node : CstNode> CstParseContext.node(factory: CstNodeFactory<Node>): Node = rawNode(
	kind = CstParseContext.NodeKind.Node,
	getInfo = { factory.info },
	block = { with(factory) { parse() } },
	onSuccess = { it },
	onError = { throw it },
)

inline fun <Node : CstNode> CstParseContext.node(
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	kind = CstParseContext.NodeKind.Node,
	getInfo = { info },
	block = block,
	onSuccess = { it },
	onError = { throw it },
)

inline fun <reified Node : CstNode> CstParseContext.node(
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	kind = CstParseContext.NodeKind.Node,
	getInfo = { nodeInfoOf<Node>() },
	block = block,
	onSuccess = { it },
	onError = { throw it },
)

fun <Node : CstNode> CstParseContext.structuredNode(factory: CstNodeFactory<Node>): Node = rawNode(
	kind = CstParseContext.NodeKind.StructuredNode,
	getInfo = { factory.info },
	block = { with(factory) { parse() } },
	onSuccess = { it },
	onError = { throw it },
)

inline fun <Node : CstNode> CstParseContext.structuredNode(
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	kind = CstParseContext.NodeKind.StructuredNode,
	getInfo = { info },
	block = block,
	onSuccess = { it },
	onError = { throw it },
)

inline fun <reified Node : CstNode> CstParseContext.StructuredNode(
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	kind = CstParseContext.NodeKind.StructuredNode,
	getInfo = { nodeInfoOf<Node>() },
	block = block,
	onSuccess = { it },
	onError = { throw it },
)

fun <Node : CstNode> CstParseContext.discardable(factory: CstNodeFactory<Node>): Node? = rawNode(
	kind = CstParseContext.NodeKind.Discardable,
	getInfo = { factory.info },
	block = { with(factory) { parse() } },
	onSuccess = { it },
	onError = { null },
)

inline fun <Node : CstNode> CstParseContext.discardable(
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node,
): Node? = rawNode(
	kind = CstParseContext.NodeKind.Discardable,
	getInfo = { info },
	block = block,
	onSuccess = { it },
	onError = { null },
)

inline fun <reified Node : CstNode> CstParseContext.discardable(
	crossinline block: CstParseContext.() -> Node,
): Node? = rawNode(
	kind = CstParseContext.NodeKind.Discardable,
	getInfo = { nodeInfoOf<Node>() },
	block = block,
	onSuccess = { it },
	onError = { null },
)
