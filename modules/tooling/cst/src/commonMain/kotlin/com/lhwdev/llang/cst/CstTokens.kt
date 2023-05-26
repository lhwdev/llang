package com.lhwdev.llang.cst

import com.lhwdev.llang.token.CstToken


sealed class CstTokens<T>(val rawTokens: List<CstToken>) {
	class Single<T>(rawTokens: List<CstToken>) : CstTokens<T>(rawTokens)
	
	class Multiple<T>(rawTokens: List<CstToken>) : CstTokens<T>(rawTokens)
}


typealias CstTokenWs = CstTokens.Multiple<CstWhitespace>
