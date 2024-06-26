package com.lhwdev.llang.cst.structure.core

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeImpl
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl


/**
 * Node for tokens with unimportant token kind, such as `Comma`, `Dot` etc.
 */
interface CstLeafNode : CstNode {
	
	override val info: CstNodeInfo<out CstLeafNode>
		get() = Info
	
	companion object Info : CstNodeInfo<CstLeafNode> {
		override fun dummyNode() = CstLeafNodeImpl(TokenImpl.dummyIllegal())
	}
	
	
	val token: Token
	
	class Comma(token: Token) : CstLeafNodeImpl(token)
	class Dot(token: Token) : CstLeafNodeImpl(token)
	class Colon(token: Token) : CstLeafNodeImpl(token)
}

open class CstLeafNodeImpl(override val token: Token) : CstLeafNode, CstNodeImpl()
