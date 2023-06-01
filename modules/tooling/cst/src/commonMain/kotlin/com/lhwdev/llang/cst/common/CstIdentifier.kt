package com.lhwdev.llang.cst.common

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.CstNodeKind
import com.lhwdev.llang.cst.CstParseContext
import com.lhwdev.llang.cst.node


class CstIdentifier(c: CstParseContext) : CstNode {
	
	
	companion object : CstNodeKind<CstIdentifier>
}


fun CstParseContext.cstIdentifier(): CstIdentifier = node {

}
