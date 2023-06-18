package com.lhwdev.llang.parser.expression

import com.lhwdev.llang.cst.structure.core.CstLeafNode
import com.lhwdev.llang.token.Token


sealed class CstConstLiteral {
	class Number(override val token: Token) : CstConstLiteral(), CstLeafNode
}
