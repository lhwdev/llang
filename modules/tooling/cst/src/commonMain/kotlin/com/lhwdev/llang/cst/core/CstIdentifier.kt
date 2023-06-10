package com.lhwdev.llang.cst.core

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.CstNodeInfo
import com.lhwdev.llang.token.Token


class CstIdentifier(val token: Token) : CstNode {
	companion object Info : CstNodeInfo<CstIdentifier>
}
