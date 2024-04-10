package com.lhwdev.llang.cst.structure.core

import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.token.Token


class CstKeyword(token: Token) : CstLeafNodeImpl(token) {
	override val info
		get() = Info
	
	companion object Info : CstNodeInfo<CstKeyword> {
		/**
		 * This prevents nodes containing [CstKeyword] as an element to be created as a dummy
		 * element.
		 */
		override fun dummyNode() = null
	}
}
