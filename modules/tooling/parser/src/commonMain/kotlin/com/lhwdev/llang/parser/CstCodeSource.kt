package com.lhwdev.llang.parser

import com.lhwdev.llang.token.Token
import com.lhwdev.llang.tokenizer.source.CodeSource


interface CstCodeSource : CodeSource {
	/**
	 * Accepts a token derived from another parallel space. A parallel space can be created by
	 * [cloneForRead].
	 */
	fun acceptToken(token: Token): Token
	
	fun cloneForRead(): CstCodeSource
	
	fun close()
}
