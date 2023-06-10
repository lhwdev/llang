package com.lhwdev.llang.parser.util

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.util.CstList
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.discardable
import com.lhwdev.llang.parser.node


inline fun <reified Item : CstNode> CstParseContext.cstList(
	crossinline block: CstParseContext.() -> Item
): CstList<Item> = node(CstList.info()) {
	val list = mutableListOf<Item>()
	while(true) {
		val item = discardable { block() }
		if(item != null) {
			list += item
		} else {
			break
		}
	}
	
	CstList(list)
}
