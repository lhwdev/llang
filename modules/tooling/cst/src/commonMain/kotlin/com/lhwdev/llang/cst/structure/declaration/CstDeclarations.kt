package com.lhwdev.llang.cst.structure.declaration

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeImpl


class CstDeclarations<out T : CstNamedDeclaration>(val declarations: List<T>) : CstNode,
	CstNodeImpl()
