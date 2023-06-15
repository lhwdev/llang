package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.core.CstModifier
import com.lhwdev.llang.cst.core.CstModifiers
import com.lhwdev.llang.parser.CstNodeFactory
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.structuredNode
import com.lhwdev.llang.parser.util.cstSeparatedList
import com.lhwdev.llang.tokenizer.parseModifier


val cstModifier = CstNodeFactory { cstModifier() }

fun CstParseContext.cstModifier(): CstModifier =
	structuredNode(CstModifier) { CstModifier(code.parseModifier()) }

fun CstParseContext.cstModifiers(): CstModifiers = structuredNode(CstModifiers) {
	CstModifiers(
		cstSeparatedList(
			itemFactory = cstModifier,
			separatorBlock = { cstWsOrNull() },
		)
	)
}
