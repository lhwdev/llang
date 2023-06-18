package com.lhwdev.llang.cst.structure.core

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstComment.Begin
import com.lhwdev.llang.cst.structure.core.CstComment.End
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl
import com.lhwdev.llang.token.TokenKinds


class CstWss(val nodes: List<CstWs>) : CstNode {
	companion object Info : CstNodeInfo<CstWss> {
		override fun dummyNode() = CstWss(emptyList())
	}
}


interface CstWs : CstNode {
	companion object Info : CstNodeInfo<CstWs> {
		override fun dummyNode() = CstWhitespace.dummyNode()
	}
}


class CstWhitespace(token: Token) : CstWs, CstLeafNodeImpl(token) {
	companion object Info : CstNodeInfo<CstWhitespace> {
		override fun dummyNode(): CstWhitespace =
			CstWhitespace(TokenImpl.dummy(TokenKinds.Whitespace, " "))
	}
}

class CstLineBreak(token: Token) : CstWs, CstLeafNodeImpl(token) {
	companion object Info : CstNodeInfo<CstLineBreak> {
		override fun dummyNode(): CstLineBreak =
			CstLineBreak(TokenImpl.dummy(TokenKinds.LineBreak, "\n"))
	}
}


/**
 * `nodes.first()` should be an instance of [Begin], and if this comment is block comment,
 * `nodes.last()` should be an instance of [End].
 */
class CstComment(val nodes: List<CstWs>) : CstWs {
	companion object Info : CstNodeInfo<CstComment> {
		override fun dummyNode(): CstComment =
			CstComment(emptyList())
	}
	
	sealed class Leaf(token: Token) : CstLeafNodeImpl(token) {
		companion object Info : CstNodeInfo<Leaf> {
			override fun dummyNode() = null
		}
	}
	
	class Begin(token: Token) : CstWs, Leaf(token) {
		companion object Info : CstNodeInfo<Begin> {
			override fun dummyNode() = Begin(TokenImpl.dummyIllegal())
		}
	}
	
	class End(token: Token) : CstWs, Leaf(token) {
		companion object Info : CstNodeInfo<End> {
			override fun dummyNode() = End(TokenImpl.dummyIllegal())
		}
	}
	
	
	class Content(token: Token) : CstWs, Leaf(token) {
		companion object Info : CstNodeInfo<Content> {
			override fun dummyNode() = Content(TokenImpl.dummyIllegal())
		}
	}
	
}
