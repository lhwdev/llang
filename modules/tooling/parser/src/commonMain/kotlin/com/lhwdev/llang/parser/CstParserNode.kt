package com.lhwdev.llang.parser

import com.lhwdev.llang.cst.tree.CstTreeNode
import com.lhwdev.llang.token.Token


interface CstParserNode : CstTreeNode {
	val kind: CstParseContext.NodeKind
	
	
	override val isLeaf: Boolean
		get() = kind == CstParseContext.NodeKind.LeafNode
}
