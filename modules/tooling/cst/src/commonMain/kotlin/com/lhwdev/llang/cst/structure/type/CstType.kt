package com.lhwdev.llang.cst.structure.type

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo


interface CstType : CstNode {
	companion object Info : CstNodeInfo<CstType> {
		override fun dummyNode() = object : CstType {}
	}
}
