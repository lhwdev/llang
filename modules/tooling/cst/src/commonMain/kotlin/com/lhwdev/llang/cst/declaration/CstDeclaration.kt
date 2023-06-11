package com.lhwdev.llang.cst.declaration

import com.lhwdev.llang.cst.core.CstIdentifier
import com.lhwdev.llang.cst.statement.CstStatement


interface CstDeclaration : CstStatement {
	val name: CstIdentifier
	
}


interface CstMemberDeclaration : CstDeclaration {
	val modality: CstModality
}
