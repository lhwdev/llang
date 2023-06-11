package com.lhwdev.llang.parser.util

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.util.CstSeparatedList
import com.lhwdev.llang.cst.util.CstSeparatedListItem
import com.lhwdev.llang.parser.CstNodeFactory
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.discardable
import com.lhwdev.llang.parser.node


fun <Item : CstNode, Separator : CstNode> CstParseContext.cstSeparatedList(
	itemFactory: CstNodeFactory<Item>,
	separatorFactory: CstNodeFactory<Separator>,
	// allowTrailing: Boolean = true // fixed to true
): CstSeparatedList<Item, Separator> = node(CstSeparatedList.info()) {
	val items = mutableListOf<CstSeparatedListItem<Item, Separator>>()
	
	while(true) {
		val item = discardable(itemFactory) ?: break
		val separator = discardable(separatorFactory)
		if(separator != null) {
			items += CstSeparatedListItem(item, separator)
		} else {
			items += CstSeparatedListItem(item, separator = null)
			break
		}
	}
	
	CstSeparatedList(items)
}