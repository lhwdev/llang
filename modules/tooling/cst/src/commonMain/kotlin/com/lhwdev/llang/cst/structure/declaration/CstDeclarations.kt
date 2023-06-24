package com.lhwdev.llang.cst.structure.declaration

import com.lhwdev.llang.cst.structure.CstNode


class CstDeclarations<out T : CstDeclaration>(val declarations: List<T>) : CstNode
