package com.lhwdev.llang.cst.type

import com.lhwdev.llang.cst.CstNodeInfo
import com.lhwdev.llang.cst.core.CstAccess


class CstTypeReference(val access: CstAccess) : CstType {
	companion object Info : CstNodeInfo<CstTypeReference> {
		override fun dummyNode() = CstTypeReference(CstAccess.dummyNode())
	}
}
