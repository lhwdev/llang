package com.lhwdev.llang.ast.declaration

import com.lhwdev.llang.ast.AstNamed
import com.lhwdev.llang.ast.AstNode


interface AstDeclaration : AstNode, AstNamed {
	val parent: AstDeclaration
}
