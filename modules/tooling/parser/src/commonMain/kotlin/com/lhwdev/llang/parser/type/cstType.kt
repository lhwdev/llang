package com.lhwdev.llang.parser.type

import com.lhwdev.llang.cst.structure.type.CstType
import com.lhwdev.llang.cst.structure.util.CstOptional
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.core.cstLeafNodeOrNull
import com.lhwdev.llang.parser.core.cstWssOrEmpty
import com.lhwdev.llang.parser.node
import com.lhwdev.llang.token.TokenKinds


fun CstParseContext.cstDeclarationQuoteType(): CstOptional<CstType> = node {
	cstLeafNodeOrNull(TokenKinds.Operator.Other.Colon, ":") ?: return@node CstOptional()
	cstWssOrEmpty()
	CstOptional(cstType())
}
