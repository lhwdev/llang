package com.lhwdev.llang.cst.util

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.CstNodeInfo


class CstList<Item : CstNode>(val items: List<Item>) : CstNode {
	companion object Item : CstNodeInfo<CstList<CstNode>> {
		@Suppress("UNCHECKED_CAST")
		fun <Item : CstNode> info(): CstNodeInfo<CstList<Item>> =
			this as CstNodeInfo<CstList<Item>>
		
		override fun dummyNode(): CstList<CstNode> =
			CstList(emptyList())
	}
}
