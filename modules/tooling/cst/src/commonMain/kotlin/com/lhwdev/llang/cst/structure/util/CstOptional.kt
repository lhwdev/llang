package com.lhwdev.llang.cst.structure.util

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeImpl
import com.lhwdev.llang.cst.structure.CstNodeInfo


class CstOptional<out T : CstNode>(val inner: T? = null) : CstNode, CstNodeImpl() {
	companion object Info : CstNodeInfo<CstOptional<*>> {
		val None = CstOptional(null)
		
		override fun dummyNode() = None
	}
}

val <T : CstNode> T?.optional: CstOptional<T>
	get() = if(this != null) CstOptional(this) else CstOptional.None
