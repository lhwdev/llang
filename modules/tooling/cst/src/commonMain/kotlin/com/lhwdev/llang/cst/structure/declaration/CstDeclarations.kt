package com.lhwdev.llang.cst.structure.declaration

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeImpl
import com.lhwdev.llang.cst.structure.CstNodeInfo


class CstDeclarations<out T : CstNamedDeclaration>(val declarations: List<T>) : CstNode,
	CstNodeImpl() {
	override val info: CstNodeInfo<out CstNode>
		get() = TODO("Not yet implemented")
}
