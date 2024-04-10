package com.lhwdev.llang.cst.structure.util

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeImpl
import com.lhwdev.llang.cst.structure.CstNodeInfo


class CstSeparatedList<out Item : CstNode, out Separator : CstNode>(
	val elements: List<CstSeparatedListItem<Item, Separator>>,
) : CstNode, CstNodeImpl() {
	override val info
		get() = Info
	
	companion object Info : CstNodeInfo<CstSeparatedList<CstNode, CstNode>> {
		@Suppress("UNCHECKED_CAST")
		fun <Item : CstNode, Separator : CstNode> info(): CstNodeInfo<CstSeparatedList<Item, Separator>> =
			this as CstNodeInfo<CstSeparatedList<Item, Separator>>
		
		override fun dummyNode(): CstSeparatedList<CstNode, CstNode> =
			CstSeparatedList(listOf())
	}
	
	fun items(): List<Item> = elements.map { it.item }
}


class CstSeparatedListItem<out Item : CstNode, out Separator : CstNode>(
	val item: Item,
	val separator: Separator?,
)
