package com.lhwdev.llang.cst.structure.declaration

import com.lhwdev.llang.cst.structure.CstNodeImpl
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.type.CstType


class CstContextDeclaration(
	val contexts: List<CstType>,
) : CstDeclaration, CstNodeImpl() {
	override val info
		get() = Info
	
	companion object Info : CstNodeInfo<CstContextDeclaration> {
		override fun dummyNode() = CstContextDeclaration(emptyList())
	}
}
