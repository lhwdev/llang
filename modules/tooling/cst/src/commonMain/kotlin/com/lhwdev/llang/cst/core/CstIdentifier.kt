package com.lhwdev.llang.cst.core

import com.lhwdev.llang.cst.CstLeafNode
import com.lhwdev.llang.cst.CstNodeInfo
import com.lhwdev.llang.token.Token


class CstIdentifier(token: Token) : CstLeafNode(token) {
	companion object Info : CstNodeInfo<CstIdentifier>
}
