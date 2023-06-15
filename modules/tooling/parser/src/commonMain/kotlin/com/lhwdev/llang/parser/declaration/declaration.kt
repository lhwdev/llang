package com.lhwdev.llang.parser.declaration

import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.declaration.CstDeclaration
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.core.cstWs
import com.lhwdev.llang.parser.structuredNode


/**
 * Uses [structuredNode]. If you need branching, wrap it in another
 * [node][com.lhwdev.llang.parser.node].
 */
inline fun <Node : CstDeclaration> CstParseContext.declaration(
	info: CstNodeInfo<Node>,
	crossinline block: CstParseContext.() -> Node,
): Node = structuredNode(info) {
	val prefixWss = cstWs()
	val declaration = block()
	
	declaration
}
