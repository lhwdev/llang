package com.lhwdev.llang.parser.declaration

import com.lhwdev.llang.cst.structure.declaration.CstContextDeclaration
import com.lhwdev.llang.cst.structure.util.CstOptional
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.core.cstLeafNodeOrNull
import com.lhwdev.llang.token.TokenKinds


fun CstParseContext.cstContextDeclarationOrNone(): CstOptional<CstContextDeclaration> {
	if(cstLeafNodeOrNull(TokenKinds.Identifier.Simple, "context") != null) TODO()
	return CstOptional.None
}
