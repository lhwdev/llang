package com.lhwdev.llang.cst


interface CstNode

interface CstNodeKind<Node : CstNode>

typealias CstNodeFactory<Node> = CstParseContext.() -> Node
