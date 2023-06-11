package com.lhwdev.llang.cst.declaration

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.CstNodeInfo
import com.lhwdev.llang.cst.core.CstModifier
import com.lhwdev.llang.cst.core.CstWs
import com.lhwdev.llang.cst.util.CstSeparatedList


class CstModality(val modifiers: CstSeparatedList<CstModifier, CstWs>) : CstNode {
	companion object Info : CstNodeInfo<CstModality> {
		override fun dummyNode() = CstModality(CstSeparatedList(emptyList()))
	}
}
