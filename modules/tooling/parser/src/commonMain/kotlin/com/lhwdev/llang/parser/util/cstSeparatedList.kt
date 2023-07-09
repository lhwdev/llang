package com.lhwdev.llang.parser.util

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstLeafNode
import com.lhwdev.llang.cst.structure.util.CstSeparatedList
import com.lhwdev.llang.cst.structure.util.CstSeparatedListItem
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.core.cstLeafCommaOrNull
import com.lhwdev.llang.parser.discardable
import com.lhwdev.llang.parser.node


inline fun <Item : CstNode, Separator : CstNode> CstParseContext.cstSeparatedList(
	info: CstNodeInfo<Item>?,
	crossinline itemBlock: CstParseContext.() -> Item,
	crossinline separatorBlock: CstParseContext.() -> Separator?,
	// allowTrailing: Boolean = true // fixed to true
): CstSeparatedList<Item, Separator> = node(CstSeparatedList.info()) {
	val items = mutableListOf<CstSeparatedListItem<Item, Separator>>()
	
	while(true) {
		val item = discardable(info, itemBlock) ?: break
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

inline fun <Item : CstNode> CstParseContext.cstCommaSeparatedList(
	info: CstNodeInfo<Item>,
	crossinline itemBlock: CstParseContext.() -> Item,
): CstSeparatedList<Item, CstLeafNode.Comma> = cstSeparatedList(
	info = info,
	itemBlock = itemBlock,
	separatorBlock = { cstLeafCommaOrNull() },
)
