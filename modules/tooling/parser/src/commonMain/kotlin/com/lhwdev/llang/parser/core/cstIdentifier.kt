package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.core.CstIdentifier
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.structuredNode
import com.lhwdev.llang.tokenizer.parseIdentifier


fun CstParseContext.cstIdentifier(): CstIdentifier =
	structuredNode(CstIdentifier) { CstIdentifier(code.parseIdentifier()) }
