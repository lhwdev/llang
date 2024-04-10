package com.lhwdev.llang.cst.structure

import com.lhwdev.llang.cst.tree.CstTreeNode
import com.lhwdev.utils.platform.CallContextLocal


private val Stub = object : CstLocalTreeProvider {
	override val tree: CstTreeNode? get() = null
}
val LocalCstTreeProvider = CallContextLocal<CstTreeProvider>(
	object : CstTreeProvider {
		override fun local(): CstLocalTreeProvider = Stub
	},
)


abstract class CstNodeImpl : CstNode {
	private var _tree: CstTreeNode? = null
	private val provider: CstLocalTreeProvider = LocalCstTreeProvider.current.local()
	
	override val tree: CstTreeNode
		get() = _tree ?: run {
			val got = provider.tree
			_tree = got
			if(got == null) {
				error("tree is null for $this; provider=${provider}")
			}
			got
		}
	
	@Deprecated(level = DeprecationLevel.HIDDEN, message = "do not implement")
	override val _doNotImplementDirectly: DoNotImplementDirectly
		get() = _doNotImplementDirectlyVal
}


interface CstTreeProvider {
	fun local(): CstLocalTreeProvider
}

interface CstLocalTreeProvider {
	val tree: CstTreeNode?
}
