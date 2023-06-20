package com.lhwdev.llang.parser

import com.lhwdev.llang.token.Token
import com.lhwdev.llang.tokenizer.source.CodeSource


interface CstCodeSource : CodeSource {
	fun acceptToken(token: Token): Token
	
	fun cloneForRead(): CstCodeSource
	
	fun close()
}
