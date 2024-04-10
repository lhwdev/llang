package com.lhwdev.llang.parser.util

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.util.CstSurround
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.core.cstLeafNode
import com.lhwdev.llang.parser.structuredNode


inline fun <Node : CstNode> CstParseContext.cstSurround(
	kind: CstSurround.Kind,
	crossinline block: CstParseContext.() -> Node,
): CstSurround<Node> = structuredNode {
	cstLeafNode(kind.left, kind.leftContent)
	val content = block()
	cstLeafNode(kind.right, kind.rightContent)
	
	CstSurround(kind, content)
}
