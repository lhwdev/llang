package com.lhwdev.llang.cst.structure

import com.lhwdev.llang.cst.tree.CstTreeNode

open class CstNodeImpl : CstNode {
	private var _tree: CstTreeNode? = null
	
	override val tree: CstTreeNode
		get() = _tree ?: error("tree is null for $this")
	
	override fun attachTree(node: CstTreeNode) {
		_tree = node
	}
	
	@Deprecated(level = DeprecationLevel.HIDDEN, message = "do not implement")
	override val _doNotImplementDirectly: DoNotImplementDirectly
		get() = _doNotImplementDirectlyVal
}
