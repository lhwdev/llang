package com.lhwdev.llang.ast.declaration


interface AstPackage : AstDeclaration, AstPackageElement {
	var children: List<AstPackageElement>
}

interface AstPackageElement : AstDeclaration
