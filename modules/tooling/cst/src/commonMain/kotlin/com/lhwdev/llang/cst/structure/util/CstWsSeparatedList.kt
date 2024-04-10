package com.lhwdev.llang.cst.structure.util

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeImpl
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.util.CstWsSeparatedList.Item


class CstWsSeparatedList<out Item : CstNode>(items: List<Item>) : CstNode, List<Item> by items,
	CstNodeImpl() {
	override val info: CstNodeInfo<CstWsSeparatedList<*>>
		get() = Item
	
	companion object Item : CstNodeInfo<CstWsSeparatedList<CstNode>> {
		@Suppress("UNCHECKED_CAST")
		fun <Item : CstNode> info(): CstNodeInfo<CstWsSeparatedList<Item>> =
			this as CstNodeInfo<CstWsSeparatedList<Item>>
		
		override fun dummyNode(): CstWsSeparatedList<CstNode> =
			CstWsSeparatedList(emptyList())
	}
}
