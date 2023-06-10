package com.lhwdev.llang.cst

import com.lhwdev.llang.token.Token


/**
 * Node for tokens with unimportant token kind, such as `Comma`, `Dot` etc.
 */
class CstLeafNode(val token: Token) : CstNode {
	companion object Info : CstNodeInfo<CstLeafNode>
}
