package com.lhwdev.llang.cst.structure


interface CstNode {
	val childNodes: List<CstNode>? get() = null
	
	companion object Info : CstNodeInfo<CstNode> {
		override fun dummyNode() = object : CstNode {
			override val childNodes: List<CstNode>
				get() = emptyList()
		}
	}
}
