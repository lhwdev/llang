package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.core.CstModifier
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.node
import com.lhwdev.llang.tokenizer.parseModifier


fun CstParseContext.cstModifier(): CstModifier =
	node(CstModifier) { CstModifier(code.parseModifier()) }
