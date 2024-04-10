package com.lhwdev.llang.cst.structure.expression

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeImpl
import com.lhwdev.llang.cst.structure.CstNodeInfo


interface CstExpression : CstNode {
	override val info: CstNodeInfo<out CstExpression>
		get() = Info
	
	companion object Info : CstNodeInfo<CstExpression> {
		override fun dummyNode(): CstExpression = object : CstExpression, CstNodeImpl() {}
	}
}
