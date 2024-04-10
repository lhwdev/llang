package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.structure.core.CstIdentifier
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.leafNode
import com.lhwdev.llang.tokenizer.parseIdentifier


fun CstParseContext.cstIdentifier(): CstIdentifier =
	leafNode { CstIdentifier(code.parseIdentifier()) }
