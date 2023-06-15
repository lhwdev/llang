package com.lhwdev.llang.cst.util

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.CstNodeInfo


class CstWsSeparatedList<Item : CstNode>(items: List<Item>) : CstNode, List<Item> by items {
	companion object Item : CstNodeInfo<CstWsSeparatedList<CstNode>> {
		@Suppress("UNCHECKED_CAST")
		fun <Item : CstNode> info(): CstNodeInfo<CstWsSeparatedList<Item>> =
			this as CstNodeInfo<CstWsSeparatedList<Item>>
		
		override fun dummyNode(): CstWsSeparatedList<CstNode> =
			CstWsSeparatedList(emptyList())
	}
}
