package com.lhwdev.llang.cst

import com.lhwdev.llang.token.Token


sealed class CstTokens<T>(val rawTokens: List<Token>) {
	class Single<T>(rawTokens: List<Token>) : CstTokens<T>(rawTokens)
	
	class Multiple<T>(rawTokens: List<Token>) : CstTokens<T>(rawTokens)
}


typealias CstTokenWs = CstTokens.Multiple<CstWhitespace>
