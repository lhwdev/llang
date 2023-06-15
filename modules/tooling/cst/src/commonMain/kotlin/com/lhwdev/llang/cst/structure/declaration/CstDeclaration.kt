package com.lhwdev.llang.cst.structure.declaration

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.core.CstIdentifier
import com.lhwdev.llang.cst.structure.core.CstModifiers
import com.lhwdev.llang.cst.structure.statement.CstStatement


interface CstDeclarationLike : CstNode


interface CstDeclaration : CstDeclarationLike, CstStatement {
	val annotations: CstAnnotations
	
	val modifiers: CstModifiers
	
	val name: CstIdentifier
}


interface CstAccessibleDeclaration : CstDeclaration

interface CstStandaloneDeclaration : CstAccessibleDeclaration

interface CstMemberDeclaration : CstAccessibleDeclaration

interface CstLocalDeclaration : CstDeclaration
