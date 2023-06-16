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
	beginNode<Node>(kind)?.let { return it }
	val nodeGroupId = currentNodeGroupId
	return try {
		val node = try {
			block()
		} finally {
			beforeEndNodeDebugHint(nodeGroupId)
		}
		if(node != null) {
			endNode(node)
		} else {
			endNodeWithError(throwable = null, info = null)
		}
	} catch(throwable: Throwable) {
		endNodeWithError(throwable, info) ?: throw throwable
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
