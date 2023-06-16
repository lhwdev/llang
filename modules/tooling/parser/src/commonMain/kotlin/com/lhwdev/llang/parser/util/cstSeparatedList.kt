package com.lhwdev.llang.parser.util

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.util.CstSeparatedList
import com.lhwdev.llang.cst.structure.util.CstSeparatedListItem
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.discardable
import com.lhwdev.llang.parser.node


inline fun <reified Item : CstNode, Separator : CstNode> CstParseContext.cstSeparatedList(
	crossinline itemBlock: CstParseContext.() -> Item,
	crossinline separatorBlock: CstParseContext.() -> Separator?,
	// allowTrailing: Boolean = true // fixed to true
): CstSeparatedList<Item, Separator> = node(CstSeparatedList.info()) {
	val items = mutableListOf<CstSeparatedListItem<Item, Separator>>()
	
	while(true) {
		val item = discardable(itemBlock) ?: break
		val separator = separatorBlock()
		if(separator != null) {
			items += CstSeparatedListItem(item, separator)
		} else {
			items += CstSeparatedListItem(item, separator = null)
			break
		}
	}
	
	CstSeparatedList(items)
}
