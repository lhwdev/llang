package com.lhwdev.llang.parser

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.CstNodeInfo
import com.lhwdev.llang.cst.nodeInfoOf


@OptIn(CstParseContext.InternalApi::class)
inline fun <Node : CstNode, Return> CstParseContext.rawNode(
	kind: CstParseContext.NodeKind,
	getInfo: () -> CstNodeInfo<Node>?,
	block: CstParseContext.() -> Node,
	onSuccess: (node: Node) -> Return, // workaround for forbidden `Node : CstNode, Node : Return`.
	onError: (throwable: Throwable) -> Return,
): Return {
	beginNode<Node>(kind)?.let { return onSuccess(it) }
	val nodeGroupId = currentNodeGroupId
	return try {
		val node = try {
			block()
		} finally {
			beforeEndNodeDebugHint(nodeGroupId)
		}
		onSuccess(endNode(node))
	} catch(throwable: Throwable) {
		val dummy = endNodeWithError(throwable, getInfo())
		if(dummy != null) {
			onSuccess(dummy)
		} else {
			onError(throwable)
		}
	}
}


fun <Node : CstNode> CstParseContext.node(factory: CstNodeFactory<Node>): Node = rawNode(
	CstParseContext.NodeKind.Node,
	getInfo = { factory.info },
	block = { with(factory) { parse() } },
	onSuccess = { it },
	onError = { throw it },
)

inline fun <Node : CstNode> CstParseContext.node(
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	CstParseContext.NodeKind.Node,
	getInfo = { info },
	block = block,
	onSuccess = { it },
	onError = { throw it },
)

inline fun <reified Node : CstNode> CstParseContext.node(
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	CstParseContext.NodeKind.Node,
	getInfo = { nodeInfoOf<Node>() },
	block = block,
	onSuccess = { it },
	onError = { throw it },
)

fun <Node : CstNode> CstParseContext.structuredNode(factory: CstNodeFactory<Node>): Node = rawNode(
	CstParseContext.NodeKind.StructuredNode,
	getInfo = { factory.info },
	block = { with(factory) { parse() } },
	onSuccess = { it },
	onError = { throw it },
)

inline fun <Node : CstNode> CstParseContext.structuredNode(
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	CstParseContext.NodeKind.StructuredNode,
	getInfo = { info },
	block = block,
	onSuccess = { it },
	onError = { throw it },
)

inline fun <reified Node : CstNode> CstParseContext.StructuredNode(
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	CstParseContext.NodeKind.StructuredNode,
	getInfo = { nodeInfoOf<Node>() },
	block = block,
	onSuccess = { it },
	onError = { throw it },
)

fun <Node : CstNode> CstParseContext.discardable(factory: CstNodeFactory<Node>): Node? = rawNode(
	CstParseContext.NodeKind.Discardable,
	getInfo = { factory.info },
	block = { with(factory) { parse() } },
	onSuccess = { it },
	onError = { null },
)

inline fun <Node : CstNode> CstParseContext.discardable(
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node,
): Node? = rawNode(
	CstParseContext.NodeKind.Discardable,
	getInfo = { info },
	block = block,
	onSuccess = { it },
	onError = { null },
)

inline fun <reified Node : CstNode> CstParseContext.discardable(
	crossinline block: CstParseContext.() -> Node,
): Node? = rawNode(
	CstParseContext.NodeKind.Discardable,
	getInfo = { nodeInfoOf<Node>() },
	block = block,
	onSuccess = { it },
	onError = { null },
)
