package com.lhwdev.llang.parser

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.nodeInfoOf

interface CstNodeFactory<Node : CstNode> {
	val info: CstNodeInfo<Node>?
	
	fun CstParseContext.parse(): Node
}

inline fun <reified Node : CstNode> CstNodeFactory(
	crossinline block: CstParseContext.() -> Node,
): CstNodeFactory<Node> = object : CstNodeFactory<Node> {
	override val info: CstNodeInfo<Node> = nodeInfoOf<Node>()!!
	
	override fun CstParseContext.parse(): Node = block()
}
