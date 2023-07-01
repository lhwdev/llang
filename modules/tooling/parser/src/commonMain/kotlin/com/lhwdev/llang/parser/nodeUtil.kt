package com.lhwdev.llang.parser

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstLeafNode


inline fun <Node : CstNode> CstParseContext.discardable(
	info: CstNodeInfo<Node>?, // 어라 생각해보니 info 안쓰는데 몰라 일단 둬 beginChildNode에 드갈수도
	crossinline block: CstParseContext.() -> Node,
): Node? = rawNullableNode(
	kind = CstParseContext.NodeKind.Discardable,
	block = block,
	onError = @OptIn(CstParseContext.InternalApi::class) { context, _ ->
		endChildNodeWithError(context, throwable = null, info = null)
	},
)

inline fun <reified Node : CstNode> CstParseContext.discardable(
	crossinline block: CstParseContext.() -> Node,
): Node? = rawNullableNode(
	kind = CstParseContext.NodeKind.Discardable,
	block = block,
	onError = @OptIn(CstParseContext.InternalApi::class) { context, _ ->
		endChildNodeWithError(context, throwable = null, info = null)
	},
)

inline fun <Node : CstNode> CstParseContext.nullableNode(
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node?,
): Node? = rawNullableNode(
	kind = CstParseContext.NodeKind.Node,
	block = block,
	onError = @OptIn(CstParseContext.InternalApi::class) { context, throwable ->
		endChildNodeWithError(context, throwable, info) ?: throw throwable
	},
)

inline fun <Node : CstNode> CstParseContext.nullableStructuredNode(
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node?,
): Node? = rawNullableNode(
	kind = CstParseContext.NodeKind.StructuredNode,
	block = block,
	onError = @OptIn(CstParseContext.InternalApi::class) { context, throwable ->
		endChildNodeWithError(context, throwable, info) ?: throw throwable
	},
)

inline fun <Node : CstLeafNode> CstParseContext.nullableLeafNode(
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node?,
): Node? = rawNullableNode(
	kind = CstParseContext.NodeKind.LeafNode,
	block = block,
	onError = @OptIn(CstParseContext.InternalApi::class) { context, throwable ->
		endChildNodeWithError(context, throwable, info) ?: throw throwable
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
