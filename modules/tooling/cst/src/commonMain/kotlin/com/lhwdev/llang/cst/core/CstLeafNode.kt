package com.lhwdev.llang.cst.core

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.token.Token


/**
 * Node for tokens with unimportant token kind, such as `Comma`, `Dot` etc.
 */
open class CstLeafNode(val token: Token) : CstNode {
	class Comma(token: Token) : CstLeafNode(token)
	class Dot(token: Token) : CstLeafNode(token)
}
