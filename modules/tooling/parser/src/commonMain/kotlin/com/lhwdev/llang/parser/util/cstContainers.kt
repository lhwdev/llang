package com.lhwdev.llang.parser.util

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.CstParseContextMarker
import com.lhwdev.llang.parsing.ParseContext
import com.lhwdev.llang.parsing.util.ParseException


@CstParseContextMarker
interface CstParsingContainerContext<Node : CstNode> : ParseContext {
	val parentContext: CstParseContext
	
	fun <Item : Node> item(block: CstParseContext.() -> Item): Item =
		itemOrNull(block) ?: parseError(
			parentContext.lastEndError as? ParseException ?: ParseException("item parsing failed"),
		)
	
	fun <Item : Node> itemOrNull(block: CstParseContext.() -> Item): Item?
}

fun <Item : Node, Node : CstNode> CstParsingContainerContext<Node>.items(
	block: CstParseContext.() -> Item,
): List<Item> {
	val items = mutableListOf<Item>()
	while(true) {
		val item = itemOrNull(block) ?: break
		items += item
	}
	return items
}
