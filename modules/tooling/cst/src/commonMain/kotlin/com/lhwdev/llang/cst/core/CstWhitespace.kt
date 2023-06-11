package com.lhwdev.llang.cst.core

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.CstNodeInfo
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl
import com.lhwdev.llang.token.TokenKinds


abstract class CstWs : CstNode {
	companion object Info : CstNodeInfo<CstWs> {
		override fun dummyNode() = CstWhitespace.dummyNode()
	}
}


class CstWhitespace(val token: Token) : CstWs() {
	companion object Info : CstNodeInfo<CstWhitespace> {
		override fun dummyNode(): CstWhitespace =
			CstWhitespace(TokenImpl.dummy(TokenKinds.Whitespace, " "))
	}
}

class CstLineBreak(val token: Token) : CstWs() {
	companion object Info : CstNodeInfo<CstLineBreak> {
		override fun dummyNode(): CstLineBreak =
			CstLineBreak(TokenImpl.dummy(TokenKinds.LineBreak, "\n"))
	}
}


class CstComment(val nodes: List<CstWs>) : CstWs() {
	companion object Info : CstNodeInfo<CstComment> {
		override fun dummyNode(): CstComment =
			CstComment(emptyList())
	}
	
	class Begin(val token: Token) : CstWs()
	
	class End(val token: Token) : CstWs()
	
	class Content(val token: Token) : CstWs()
}
