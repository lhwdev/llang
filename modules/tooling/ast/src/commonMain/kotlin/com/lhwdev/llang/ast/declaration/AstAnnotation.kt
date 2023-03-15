package com.lhwdev.llang.ast.declaration

import com.lhwdev.llang.ast.AstNode


interface AstAnnotation : AstNode {
	val call: AstCall
}
