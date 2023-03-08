package com.lhwdev.llang.ast.declaration

import com.lhwdev.llang.ast.AstNamed
import com.lhwdev.llang.ast.AstNode
import com.lhwdev.llang.ast.expression.AstStatement
import com.lhwdev.llang.common.Modality


interface AstDeclaration : AstNode, AstNamed {
	val parent: AstDeclaration
}

interface AstTopLevelDeclaration

interface AstEnclosedDeclaration

interface AstMemberDeclaration : AstEnclosedDeclaration {
	val modality: Modality
}

interface AstLocalDeclaration : AstDeclaration, AstStatement
