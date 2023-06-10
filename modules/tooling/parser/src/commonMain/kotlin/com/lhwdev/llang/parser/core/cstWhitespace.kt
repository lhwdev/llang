package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.core.CstWhitespace
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.node
import com.lhwdev.llang.parser.nullableNode
import com.lhwdev.llang.tokenizer.parseWhitespace


fun CstParseContext.cstWhitespace(): CstWhitespace =
	node { CstWhitespace(code.parseWhitespace() ?: discard()) }

fun CstParseContext.cstWhitespaceOrNull(): CstWhitespace? =
	nullableNode(CstWhitespace) { code.parseWhitespace()?.let { CstWhitespace(it) } }
