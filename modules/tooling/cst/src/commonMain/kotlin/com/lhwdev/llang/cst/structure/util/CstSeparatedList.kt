package com.lhwdev.llang.cst.structure.util

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo


class CstSeparatedList<Item : CstNode, Separator : CstNode>(
	val elements: List<CstSeparatedListItem<Item, Separator>>,
) : CstNode {
	companion object Info : CstNodeInfo<CstSeparatedList<CstNode, CstNode>> {
		@Suppress("UNCHECKED_CAST")
		fun <Item : CstNode, Separator : CstNode> info(): CstNodeInfo<CstSeparatedList<Item, Separator>> =
			this as CstNodeInfo<CstSeparatedList<Item, Separator>>
		
		override fun dummyNode(): CstSeparatedList<CstNode, CstNode> =
			CstSeparatedList(listOf())
	}
	
	fun items(): List<Item> = elements.map { it.item }
}


class CstSeparatedListItem<Item : CstNode, Separator : CstNode>(
	val item: Item,
	val separator: Separator?,
)