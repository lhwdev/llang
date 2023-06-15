package com.lhwdev.llang.cst.structure.declaration

import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.type.CstType


class CstContextDeclaration(
	val contexts: List<CstType>,
) : CstDeclarationLike {
	companion object Info : CstNodeInfo<CstContextDeclaration> {
		override fun dummyNode() = CstContextDeclaration(emptyList())
	}
}
