package com.lhwdev.llang.cst.util

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.CstNodeInfo


class CstOptional<out T : CstNode>(val inner: T? = null) : CstNode {
	companion object Info : CstNodeInfo<CstOptional<*>> {
		override fun dummyNode() = CstOptional(null)
	}
}
