package com.lhwdev.llang.cst.declaration

import com.lhwdev.llang.cst.core.CstIdentifier
import com.lhwdev.llang.cst.statement.CstStatement


interface CstDeclaration : CstStatement {
	val name: CstIdentifier
	
}


interface CstAccessibleDeclaration : CstDeclaration {
	val visibility: CstVisibility
}

interface CstMemberDeclaration : CstAccessibleDeclaration {
	val modality: CstModality
}
