package com.lhwdev.llang.parser.util

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.util.CstOptional
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.discardable


inline fun <Node : CstNode> CstParseContext.cstOptional(
	info: CstNodeInfo<Node>,
	crossinline block: CstParseContext.() -> Node,
): CstOptional<Node> = CstOptional(discardable(info, block))
