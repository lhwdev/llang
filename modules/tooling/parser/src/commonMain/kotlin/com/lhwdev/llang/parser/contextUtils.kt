package com.lhwdev.llang.parser

import com.lhwdev.llang.cst.*


@OptIn(CstParseContext.InternalApi::class)
fun <Node : CstNode> CstParseContext.node(
	factory: CstNodeFactory<Node>
): Node {
	beginNode<Node>()?.let { return it }
	val nodeGroupId = currentNodeGroupId
	return try {
		val node = try {
			with(factory) { parse() }
		} finally {
			beforeEndNodeDebugHint(nodeGroupId)
		}
		endNode(node)
	} catch(throwable: Throwable) {
		endNodeWithError(throwable, factory.info) ?: throw throwable
	}
}

@OptIn(CstParseContext.InternalApi::class)
inline fun <Node : CstNode> CstParseContext.node(
	info: CstNodeInfo<Node>,
	crossinline block: CstParseContext.() -> Node
): Node {
	beginNode<Node>()?.let { return it }
	val nodeGroupId = currentNodeGroupId
	return try {
		val node = try {
			block()
		} finally {
			beforeEndNodeDebugHint(nodeGroupId)
		}
		endNode(node)
	} catch(throwable: Throwable) {
		endNodeWithError(throwable, info) ?: throw throwable
	}
}

@OptIn(CstParseContext.InternalApi::class)
inline fun <reified Node : CstNode> CstParseContext.node(
	crossinline block: CstParseContext.() -> Node
): Node {
	beginNode<Node>()?.let { return it }
	val nodeGroupId = currentNodeGroupId
	return try {
		val node = try {
			block()
		} finally {
			beforeEndNodeDebugHint(nodeGroupId)
		}
		endNode(node)
	} catch(throwable: Throwable) {
		endNodeWithError(throwable, nodeInfoOf<Node>()) ?: throw throwable
	}
}

/**
 * Basic primitive for implementing [CstList][com.lhwdev.llang.cst.util.CstList],
 * [CstSelect][com.lhwdev.llang.cst.common.util.CstSelect] etc.
 * Useful for branching such as: 'possible patterns: [A, B] or [C, D, E]'.
 */
@OptIn(CstParseContext.InternalApi::class)
fun <Node : CstNode> CstParseContext.discardable(
	factory: CstNodeFactory<Node>
): Node? {
	beginDiscardableNode<Node>()?.let { return it }
	val nodeGroupId = currentNodeGroupId
	return try {
		val node = try {
			with(factory) { parse() }
		} finally {
			beforeEndNodeDebugHint(nodeGroupId)
		}
		endDiscardableNode(node)
	} catch(throwable: Throwable) {
		endDiscardableNodeWithError(throwable, factory.info)
	}
}

@OptIn(CstParseContext.InternalApi::class)
inline fun <Node : CstNode> CstParseContext.discardable(
	info: CstNodeInfo<Node>,
	block: CstParseContext.() -> Node,
): Node? {
	beginDiscardableNode<Node>()?.let { return it }
	val nodeGroupId = currentNodeGroupId
	return try {
		val node = try {
			block()
		} finally {
			beforeEndNodeDebugHint(nodeGroupId)
		}
		endDiscardableNode(node)
	} catch(throwable: Throwable) {
		endDiscardableNodeWithError(throwable, info)
	}
}

@OptIn(CstParseContext.InternalApi::class)
inline fun <reified Node : CstNode> CstParseContext.discardable(
	block: CstParseContext.() -> Node
): Node? {
	beginDiscardableNode<Node>()?.let { return it }
	val nodeGroupId = currentNodeGroupId
	return try {
		val node = try {
			block()
		} finally {
			beforeEndNodeDebugHint(nodeGroupId)
		}
		endDiscardableNode(node)
	} catch(throwable: Throwable) {
		endDiscardableNodeWithError(throwable, nodeInfoOf<Node>())
	}
}

fun <Node : CstNode> CstParseContext.nodeOrNull(
	factory: CstNodeFactory<Node>
): Node? = discardable(factory)

fun <Node : CstNode> CstParseContext.oneOf(
	info: CstNodeInfo<Node>,
	vararg factories: CstNodeFactory<Node>
): Node = node(info) {
	for(factory in factories) {
		val node = discardable(factory)
		if(node != null) return@node node
	}
	
	discard()
}
