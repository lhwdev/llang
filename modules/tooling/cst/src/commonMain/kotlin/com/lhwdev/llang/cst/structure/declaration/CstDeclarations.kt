package com.lhwdev.llang.cst.structure.declaration

import com.lhwdev.llang.cst.structure.CstNode


class CstDeclarations<out T : CstNamedDeclaration>(val declarations: List<T>) : CstNode
