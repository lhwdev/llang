package com.lhwdev.llang.parser.util

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.util.CstList
import com.lhwdev.llang.cst.util.CstListItem
import com.lhwdev.llang.parser.CstNodeFactory
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.discardable
import com.lhwdev.llang.parser.node


fun <Item : CstNode, Separator : CstNode> CstParseContext.cstList(
	itemFactory: CstNodeFactory<Item>,
	separatorFactory: CstNodeFactory<Separator>,
	// allowTrailing: Boolean = true // fixed to true
): CstList<Item, Separator> = node {
	val items = mutableListOf<CstListItem<Item, Separator>>()
	
	while(true) {
		val item = discardable(itemFactory) ?: break
		val separator = discardable(separatorFactory)
		if(separator != null) {
			items += CstListItem(item, separator)
		} else {
			items += CstListItem(item, separator = null)
			break
		}
	}
	
	CstList(items)
}
