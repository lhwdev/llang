package com.lhwdev.llang.cst.common

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.CstNodeInfo
import com.lhwdev.llang.cst.CstParseContext
import com.lhwdev.llang.cst.node
import com.lhwdev.llang.tokenizer.parseIdentifier


class CstIdentifier(c: CstParseContext) : CstNode {
	val token = c.code.parseIdentifier()
	
	companion object Info : CstNodeInfo<CstIdentifier>
}


fun CstParseContext.cstIdentifier(): CstIdentifier = node {
	CstIdentifier(this)
}
