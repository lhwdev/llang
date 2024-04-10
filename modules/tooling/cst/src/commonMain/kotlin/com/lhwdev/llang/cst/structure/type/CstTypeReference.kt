package com.lhwdev.llang.cst.structure.type

import com.lhwdev.llang.cst.structure.CstNodeImpl
import com.lhwdev.llang.cst.structure.CstNodeInfo


class CstTypeReference(val access: CstTypeAccess) : CstType, CstNodeImpl() {
	override val info
		get() = Info
	
	companion object Info : CstNodeInfo<CstTypeReference> {
		override fun dummyNode() = CstTypeReference(CstTypeAccess.dummyNode())
	}
}
