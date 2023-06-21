package com.lhwdev.llang.parser

import com.lhwdev.llang.token.Token
import com.lhwdev.llang.tokenizer.source.CodeSource


interface CstCodeSource : CodeSource {
	/**
	 * Accepts a token derived from another parallel space. A parallel space can be created by
	 * [cloneForRead].
	 *
	 * In [restartableNode], this has additional behavior. If the token refers to the previous
	 * token, the system maps previous token to new token.
	 */
	fun acceptToken(token: Token): Token
	
	fun cloneForRead(): CstCodeSource
	
	fun close()
}
