package com.lhwdev.llang.test.parser

import com.lhwdev.llang.parser.CstParserNode


class Groups {
	private val list = mutableListOf<CstParserNode>()
	
	
	val stack: List<CstParserNode>
		get() = list
	
	
	fun push(node: CstParserNode) {
		list.addLast(node)
	}
}
