package com.lhwdev.llang.cst.structure.expression

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeImpl
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstIdentifier
import com.lhwdev.llang.cst.structure.core.CstLeafNode
import com.lhwdev.llang.cst.structure.core.CstLeafNodeImpl
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl
import com.lhwdev.llang.token.TokenKinds


sealed class CstConstLiteral : CstExpression, CstNodeImpl() {
	class Number(override val token: Token) : CstConstLiteral(), CstLeafNode {
		override val info
			get() = Info
		
		
		companion object Info : CstNodeInfo<Number> {
			override fun dummyNode() = Number(TokenImpl.dummyIllegal())
		}
		
		override fun toString() = "CstConstLiteral.Number(${token.code})"
	}
	
	class String(val nodes: List<Element>) : CstConstLiteral() {
		override val info
			get() = Info
		
		companion object Info : CstNodeInfo<String> {
			override fun dummyNode() = String(emptyList())
		}
		
		sealed interface Element : CstNode
		
		class Begin(token: Token) : Element, CstLeafNodeImpl(token) {
			val quote: TokenKinds.StringLiteral.Quote
				get() = (token.kind as TokenKinds.StringLiteral.QuoteBegin).quote
		}
		
		class Content(token: Token) : Element, CstLeafNodeImpl(token)
		
		class TemplateVariable(val identifier: CstIdentifier) : Element, CstNodeImpl() {
			override val info: CstNodeInfo<out CstNode>
				get() = TODO("Not yet implemented")
		}
		
		class TemplateExpression(val expression: CstExpression) : Element, CstNodeImpl() {
			override val info: CstNodeInfo<out CstNode>
				get() = TODO("Not yet implemented")
		}
		
		class End(token: Token) : Element, CstLeafNodeImpl(token) {
			override val info: CstNodeInfo<out CstLeafNode>
				get() = TODO("Not yet implemented")
		}
	}
}
