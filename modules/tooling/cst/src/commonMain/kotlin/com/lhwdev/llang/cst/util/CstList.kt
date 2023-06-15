package com.lhwdev.llang.cst.util

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.CstNodeInfo


class CstList<Item : CstNode>(items: List<Item>) : CstNode, List<Item> by items {
	companion object Item : CstNodeInfo<CstList<CstNode>> {
		@Suppress("UNCHECKED_CAST")
		fun <Item : CstNode> info(): CstNodeInfo<CstList<Item>> =
			this as CstNodeInfo<CstList<Item>>
		
		override fun dummyNode(): CstList<CstNode> =
			CstList(emptyList())
	}
}
