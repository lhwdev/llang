package com.lhwdev.llang.cst.structure


interface CstNode {
	companion object Info : CstNodeInfo<CstNode> {
		override fun dummyNode() = object : CstNode {}
	}
}
