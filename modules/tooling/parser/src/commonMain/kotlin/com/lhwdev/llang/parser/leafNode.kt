package com.lhwdev.llang.parser

import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstLeafNode


inline fun <Node : CstLeafNode> CstParseContext.leafNode(
	info: CstNodeInfo<Node>?,
	crossinline block: CstParseContext.() -> Node,
): Node = rawNode(
	kind = CstParseContext.NodeKind.LeafNode,
	getInfo = { info },
	block = block,
	onSuccess = { it },
	onError = { throw it },
)
