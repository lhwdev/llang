package com.lhwdev.llang.cst.common

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.CstNodeFactory
import com.lhwdev.llang.cst.CstParseContext
import com.lhwdev.llang.cst.asFactory
import com.lhwdev.llang.token.TokenKinds


class CstList<Item : CstNode, Separator : CstNode>(
	c: CstParseContext,
	item: CstNodeFactory<Item>,
	separator: CstNodeFactory<Separator>,
	trailingAllowed: Boolean = true
) : CstNode {
	val tokens = c.cstParseTokensList(item, separator)
	
	private fun CstParseContext.cstParseTokensList(
		item: CstNodeFactory<Item>,
		separator: CstNodeFactory<Separator>
	) {
	
	}
}


fun <Item : CstNode> CstCommaList(c: CstParseContext, item: CstNodeFactory<Item>) =
	CstList(c, item, TokenKinds.Operation.Other.Comma.asFactory())
