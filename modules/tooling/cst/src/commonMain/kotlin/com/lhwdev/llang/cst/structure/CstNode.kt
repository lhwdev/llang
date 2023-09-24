package com.lhwdev.llang.cst.structure

import com.lhwdev.llang.cst.tree.CstTreeNode



interface CstNode {
	val tree: CstTreeNode
	
	fun attachTree(node: CstTreeNode)
	
	
	@Deprecated(level = DeprecationLevel.HIDDEN, message = "do not implement")
	val _doNotImplementDirectly: DoNotImplementDirectly
	
	companion object Info : CstNodeInfo<CstNode> {
		override fun dummyNode(): CstNode = object : CstNodeImpl() {}
	}
}

class DoNotImplementDirectly internal constructor()

internal val _doNotImplementDirectlyVal = DoNotImplementDirectly()
