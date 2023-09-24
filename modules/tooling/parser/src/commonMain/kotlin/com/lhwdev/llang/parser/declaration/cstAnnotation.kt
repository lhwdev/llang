package com.lhwdev.llang.parser.declaration

import com.lhwdev.llang.cst.structure.declaration.CstAnnotations
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.core.cstLeafNodeOrNull
import com.lhwdev.llang.token.TokenKinds


fun CstParseContext.cstAnnotations(): CstAnnotations {
	if(cstLeafNodeOrNull(TokenKinds.Illegal, "@") != null) {
		TODO()
	}
	return CstAnnotations(emptyList())
}
