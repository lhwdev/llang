package com.lhwdev.llang.cst.declaration

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.core.CstIdentifier
import com.lhwdev.llang.cst.core.CstModifiers
import com.lhwdev.llang.cst.core.CstWss
import com.lhwdev.llang.cst.statement.CstStatement


interface CstDeclarationLike : CstNode


interface CstDeclaration : CstDeclarationLike, CstStatement {
	var prefixWss: CstWss
	
	val modifiers: CstModifiers
	
	val name: CstIdentifier
}


interface CstAccessibleDeclaration : CstDeclaration

interface CstStandaloneDeclaration : CstAccessibleDeclaration

interface CstMemberDeclaration : CstAccessibleDeclaration

interface CstLocalDeclaration : CstDeclaration
