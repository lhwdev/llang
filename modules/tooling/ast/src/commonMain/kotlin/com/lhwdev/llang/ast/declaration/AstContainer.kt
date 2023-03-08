package com.lhwdev.llang.ast.declaration

import com.lhwdev.llang.ast.expression.AstStatement


interface AstDeclarationContainer {
	val children: List<AstDeclaration>
}

interface AstStatementContainer {
	val statements: List<AstStatement>
}
