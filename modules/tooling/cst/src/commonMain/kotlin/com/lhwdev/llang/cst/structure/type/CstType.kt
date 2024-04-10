package com.lhwdev.llang.cst.structure.type

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeImpl
import com.lhwdev.llang.cst.structure.CstNodeInfo


interface CstType : CstNode {
	override val info: CstNodeInfo<out CstType>
		get() = Info
	
	companion object Info : CstNodeInfo<CstType> {
		override fun dummyNode(): CstType = object : CstType, CstNodeImpl() {}
	}
}
