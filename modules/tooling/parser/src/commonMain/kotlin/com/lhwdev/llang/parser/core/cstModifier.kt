package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.structure.core.CstModifier
import com.lhwdev.llang.cst.structure.core.CstModifiers
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.nullableLeafNode
import com.lhwdev.llang.parser.structuredNode
import com.lhwdev.llang.parser.util.cstWsSeparatedList
import com.lhwdev.llang.tokenizer.parseModifierOrNull


fun CstParseContext.cstModifierOrNull(): CstModifier? =
	nullableLeafNode(CstModifier) { code.parseModifierOrNull()?.let { CstModifier(it) } }

fun CstParseContext.cstModifiers(): CstModifiers = structuredNode(CstModifiers) {
	CstModifiers(cstWsSeparatedList { cstModifierOrNull() })
}
