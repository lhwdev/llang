package com.lhwdev.llang.cst.structure.expression

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeImpl
import com.lhwdev.llang.cst.structure.CstNodeInfo


class CstMemberAccess(
	val parent: CstExpression,
	val item: CstNode, // CstIdentifier or CstLiteral.Integer(tuple)
) : CstExpression, CstNodeImpl() {
	override val info
		get() = Info
	
	companion object Info : CstNodeInfo<CstMemberAccess> {
		override fun dummyNode() =
			CstMemberAccess(CstExpression.dummyNode(), CstNode.dummyNode())
	}
}
