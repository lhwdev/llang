package com.lhwdev.llang.cst


inline fun <reified Node : CstNode> CstParseContext.nodeOrNull(
	crossinline block: CstNodeFactory<Node>
): Node? = discardable(block)

inline fun <reified Node : CstNode> CstParseContext.oneOf(
	vararg blocks: CstNodeFactory<Node>
): Node = node {
	for(block in blocks) {
		val node = discardable { block() }
		if(node != null) return@node node
	}
	
	discard()
}
