package com.lhwdev.llang.cst.structure

import com.lhwdev.llang.cst.tree.CstTreeNode



interface CstNode {
	val tree: CstTreeNode
	
	val info: CstNodeInfo<out CstNode>
	
	
	@Deprecated(level = DeprecationLevel.HIDDEN, message = "do not implement")
	@Suppress("PropertyName")
	val _doNotImplementDirectly: DoNotImplementDirectly
	
	companion object Info : CstNodeInfo<CstNode> {
		override fun dummyNode(): CstNode = object : CstNodeImpl() {
			override val info: CstNodeInfo<out CstNode>
				get() = Info
		}
	}
}

class DoNotImplementDirectly internal constructor()

internal val _doNotImplementDirectlyVal = DoNotImplementDirectly()
