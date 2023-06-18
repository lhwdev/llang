package com.lhwdev.llang.cst.structure.expression

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo


class CstAccessExpression(
	val parent: CstExpression,
	val item: CstNode, // CstIdentifier or CstLiteral.Integer(tuple)
) : CstExpression {
	companion object Info : CstNodeInfo<CstAccessExpression> {
		override fun dummyNode() =
			CstAccessExpression(CstExpression.dummyNode(), CstNode.dummyNode())
	}
}
