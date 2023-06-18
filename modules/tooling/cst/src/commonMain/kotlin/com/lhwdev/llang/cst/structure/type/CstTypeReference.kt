package com.lhwdev.llang.cst.structure.type

import com.lhwdev.llang.cst.structure.CstNodeInfo


class CstTypeReference(val access: CstTypeAccess) : CstType {
	companion object Info : CstNodeInfo<CstTypeReference> {
		override fun dummyNode() = CstTypeReference(CstTypeAccess.dummyNode())
	}
}
