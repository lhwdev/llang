package com.lhwdev.llang.cst.util

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.CstNodeInfo


class CstSeparatedList<Item : CstNode, Separator : CstNode>(
	val items: List<CstSeparatedListItem<Item, Separator>>
) : CstNode {
	companion object Info : CstNodeInfo<CstSeparatedList<CstNode, CstNode>> {
		@Suppress("UNCHECKED_CAST")
		fun <Item : CstNode, Separator : CstNode> info(): CstNodeInfo<CstSeparatedList<Item, Separator>> =
			this as CstNodeInfo<CstSeparatedList<Item, Separator>>
		
		override fun dummyNode(): CstSeparatedList<CstNode, CstNode> =
			CstSeparatedList(listOf())
	}
}


class CstSeparatedListItem<Item : CstNode, Separator : CstNode>(
	val item: Item,
	val separator: Separator?
)
