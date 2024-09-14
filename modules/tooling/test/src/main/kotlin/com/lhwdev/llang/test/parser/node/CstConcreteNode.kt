package com.lhwdev.llang.test.parser.node

import com.lhwdev.llang.cst.tree.CstTreeNode


interface CstConcreteNode : CstParserNode {
	override val source: CstTreeNode
		get() = this
	
	
}
