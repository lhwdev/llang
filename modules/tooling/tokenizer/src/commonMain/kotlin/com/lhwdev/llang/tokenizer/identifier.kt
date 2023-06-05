package com.lhwdev.llang.tokenizer

import com.lhwdev.llang.token.Token
import com.lhwdev.llang.tokenizer.source.CodeSource
import com.lhwdev.llang.tokenizer.source.advanceWhile


fun CodeSource.parseIdentifier(): Token = token {
	advanceWhile { }
	
	re
}
