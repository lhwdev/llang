package com.lhwdev.llang.cst.structure.type

import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstAccess


class CstTypeReference(val access: CstAccess) : CstType {
	companion object Info : CstNodeInfo<CstTypeReference> {
		override fun dummyNode() = CstTypeReference(CstAccess.dummyNode())
	}
}
