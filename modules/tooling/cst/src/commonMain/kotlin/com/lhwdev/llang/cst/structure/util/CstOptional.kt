package com.lhwdev.llang.cst.structure.util

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo


class CstOptional<out T : CstNode>(val inner: T? = null) : CstNode {
	companion object Info : CstNodeInfo<CstOptional<*>> {
		override fun dummyNode() = CstOptional(null)
	}
}
