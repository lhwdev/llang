package com.lhwdev.llang.cst.structure.expression

import com.lhwdev.llang.cst.structure.core.CstLeafNode
import com.lhwdev.llang.cst.structure.core.CstLeafNodeImpl
import com.lhwdev.llang.token.Token


sealed class CstConstLiteral : CstExpression {
	class Number(override val token: Token) : CstConstLiteral(), CstLeafNode
	
	class String(val nodes: List<Element>) : CstConstLiteral() {
		sealed class Element(token: Token) : CstLeafNodeImpl(token)
		
		class Begin(token: Token) : Element(token)
		class Content(token: Token) : Element(token)
		class End(token: Token) : Element(token)
	}
}
