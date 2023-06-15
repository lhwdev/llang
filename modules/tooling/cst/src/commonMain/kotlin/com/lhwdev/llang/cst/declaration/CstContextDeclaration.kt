package com.lhwdev.llang.cst.declaration

import com.lhwdev.llang.cst.core.CstLeafNode
import com.lhwdev.llang.cst.type.CstType
import com.lhwdev.llang.cst.util.CstSeparatedList


class CstContextDeclaration(
	val contexts: CstSeparatedList<CstType, CstLeafNode.Comma>,
) : CstDeclarationLike
