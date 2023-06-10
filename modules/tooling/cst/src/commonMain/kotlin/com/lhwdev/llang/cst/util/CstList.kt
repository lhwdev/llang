package com.lhwdev.llang.cst.util

import com.lhwdev.llang.cst.CstNode


class CstList<Item : CstNode, Separator : CstNode>(
	val items: List<CstListItem<Item, Separator>>
) : CstNode


class CstListItem<Item : CstNode, Separator : CstNode>(val item: Item, val separator: Separator?)
