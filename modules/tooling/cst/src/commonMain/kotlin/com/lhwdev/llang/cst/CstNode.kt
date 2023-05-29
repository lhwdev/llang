package com.lhwdev.llang.cst


interface CstNode

interface CstNodeKind<T : CstNode>

interface CstNodeFactory<T : CstNode> : CstNodeKind<T> {
	fun create(c: CstParseContext): T
}
