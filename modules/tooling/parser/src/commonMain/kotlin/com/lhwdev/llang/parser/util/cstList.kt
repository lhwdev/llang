package com.lhwdev.llang.parser.util

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.util.CstWsSeparatedList
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.node


inline fun <reified Item : CstNode> CstParseContext.cstWsSeparatedList(
	crossinline block: CstParseContext.() -> Item?,
): CstWsSeparatedList<Item> = node(CstWsSeparatedList.info()) { cstWsSeparatedListInline(block) }

inline fun <reified Item : CstNode> CstParseContext.cstWsSeparatedListInline(
	crossinline block: CstParseContext.() -> Item?,
): CstWsSeparatedList<Item> {
	val list = mutableListOf<Item>()
	while(true) {
		val item = block()
		if(item != null) {
			list += item
		} else {
			break
		}
	}
	
	return CstWsSeparatedList(list)
}
