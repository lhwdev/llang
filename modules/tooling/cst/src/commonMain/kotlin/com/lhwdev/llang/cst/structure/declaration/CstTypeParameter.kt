package com.lhwdev.llang.cst.structure.declaration

import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.type.CstType


class CstTypeParameters : CstDeclaration

/**
 * Such as `where T : Comparable<T>`
 */
class CstTypeParameterConstraints(val constraints: List<CstTypeParameterConstraint>) :
	CstDeclaration {
	companion object Info : CstNodeInfo<CstTypeParameterConstraints> {
		override fun dummyNode() = CstTypeParameterConstraints(emptyList())
	}
}

class CstTypeParameterConstraint(
	val target: CstType,
	val constraint: CstType,
) : CstDeclaration {
	companion object info : CstNodeInfo<CstTypeParameterConstraint> {
		override fun dummyNode() =
			CstTypeParameterConstraint(CstType.dummyNode(), CstType.dummyNode())
	}
}
