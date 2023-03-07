package com.lhwdev.llang.ast.declaration


interface AstFile : AstPackageElement {
	override val parent: AstPackage
}
