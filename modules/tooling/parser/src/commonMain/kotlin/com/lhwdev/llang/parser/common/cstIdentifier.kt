package com.lhwdev.llang.parser.common

import com.lhwdev.llang.cst.common.CstIdentifier
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.node
import com.lhwdev.llang.tokenizer.parseIdentifier


fun CstParseContext.cstIdentifier(): CstIdentifier = node {
	CstIdentifier(code.parseIdentifier())
}
