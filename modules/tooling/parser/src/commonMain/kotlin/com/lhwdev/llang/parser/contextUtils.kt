package com.lhwdev.llang.parser

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.CstNodeInfo


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
