package com.lhwdev.llang.cst.structure.declaration

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeImpl
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.type.CstType


class CstTypeParameters : CstDeclaration, CstNodeImpl() {
	override val info
		get() = CstNode.Info
	
}

/**
 * Such as `where T : Comparable<T>`
 */
class CstTypeParameterConstraints(val constraints: List<CstTypeParameterConstraint>) :
	CstDeclaration, CstNodeImpl() {
	override val info
		get() = Info
	
	companion object Info : CstNodeInfo<CstTypeParameterConstraints> {
		override fun dummyNode() = CstTypeParameterConstraints(emptyList())
	}
}

class CstTypeParameterConstraint(
	val target: CstType,
	val constraint: CstType,
) : CstDeclaration, CstNodeImpl() {
	override val info
		get() = Info
	
	companion object Info : CstNodeInfo<CstTypeParameterConstraint> {
		override fun dummyNode() =
			CstTypeParameterConstraint(CstType.dummyNode(), CstType.dummyNode())
	}
}
