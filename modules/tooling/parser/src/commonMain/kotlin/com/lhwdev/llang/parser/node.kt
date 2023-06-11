package com.lhwdev.llang.parser

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.CstNodeInfo
import com.lhwdev.llang.cst.nodeInfoOf


@OptIn(CstParseContext.InternalApi::class)
fun <Node : CstNode, Return> CstParseContext.rawNode(
	kind: CstParseContext.NodeKind,
	factory: CstNodeFactory<Node>,
	onSuccess: (node: Node) -> Return,
	onError: (throwable: Throwable) -> Return,
): Return {
	beginNode<Node>(kind)?.let { return onSuccess(it) }
	val nodeGroupId = currentNodeGroupId
	return try {
		val node = try {
			with(factory) { parse() }
		} finally {
			beforeEndNodeDebugHint(nodeGroupId)
		}
		onSuccess(endNode(node))
	} catch(throwable: Throwable) {
		val dummy = endNodeWithError(throwable, factory.info)
		if(dummy != null) {
			onSuccess(dummy)
		} else {
			onError(throwable)
		}
	}
}

@OptIn(CstParseContext.InternalApi::class)
inline fun <Node : CstNode, Return> CstParseContext.rawNode(
	kind: CstParseContext.NodeKind,
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node,
	onSuccess: (node: Node) -> Return,
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
		val dummy = endNodeWithError(throwable, info)
		if(dummy != null) {
			onSuccess(dummy)
		} else {
			onError(throwable)
		}
	}
}

@OptIn(CstParseContext.InternalApi::class)
inline fun <reified Node : CstNode, Return> CstParseContext.rawNode(
	kind: CstParseContext.NodeKind,
	crossinline block: CstParseContext.() -> Node,
	onSuccess: (node: Node) -> Return,
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
		val dummy = endNodeWithError(throwable, nodeInfoOf<Node>())
		if(dummy != null) {
			onSuccess(dummy)
		} else {
			onError(throwable)
		}
	}
}


fun <Node : CstNode> CstParseContext.node(factory: CstNodeFactory<Node>): Node =
	rawNode(CstParseContext.NodeKind.Node, factory, onSuccess = { it }, onError = { throw it })

inline fun <Node : CstNode> CstParseContext.node(
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node,
): Node =
	rawNode(CstParseContext.NodeKind.Node, info, block, onSuccess = { it }, onError = { throw it })

inline fun <reified Node : CstNode> CstParseContext.node(
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(CstParseContext.NodeKind.Node, block, onSuccess = { it }, onError = { throw it })

fun <Node : CstNode> CstParseContext.structuredNode(factory: CstNodeFactory<Node>): Node = rawNode(
	CstParseContext.NodeKind.StructuredNode,
	factory,
	onSuccess = { it },
	onError = { throw it }
)

inline fun <Node : CstNode> CstParseContext.structuredNode(
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	CstParseContext.NodeKind.StructuredNode,
	info,
	block,
	onSuccess = { it },
	onError = { throw it })

inline fun <reified Node : CstNode> CstParseContext.StructuredNode(
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	CstParseContext.NodeKind.StructuredNode,
	block,
	onSuccess = { it },
	onError = { throw it })

fun <Node : CstNode> CstParseContext.discardable(factory: CstNodeFactory<Node>): Node? = rawNode(
	CstParseContext.NodeKind.Discardable,
	factory,
	onSuccess = { it },
	onError = { null }
)

inline fun <Node : CstNode> CstParseContext.discardable(
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node,
): Node? = rawNode(
	CstParseContext.NodeKind.Discardable,
	info,
	block,
	onSuccess = { it },
	onError = { null })

inline fun <reified Node : CstNode> CstParseContext.discardable(
	crossinline block: CstParseContext.() -> Node,
): Node? = rawNode(
	CstParseContext.NodeKind.Discardable,
	block,
	onSuccess = { it },
	onError = { null }
)
