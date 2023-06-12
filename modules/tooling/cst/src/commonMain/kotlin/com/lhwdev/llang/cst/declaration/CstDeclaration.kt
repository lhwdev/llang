package com.lhwdev.llang.cst.declaration

import com.lhwdev.llang.cst.core.CstIdentifier
import com.lhwdev.llang.cst.core.CstModifiers
import com.lhwdev.llang.cst.statement.CstStatement


interface CstDeclaration : CstStatement {
	val modifiers: CstModifiers
	
	val name: CstIdentifier
}


interface CstAccessibleDeclaration : CstDeclaration

interface CstMemberDeclaration : CstAccessibleDeclaration

interface CstLocalDeclaration : CstDeclaration
