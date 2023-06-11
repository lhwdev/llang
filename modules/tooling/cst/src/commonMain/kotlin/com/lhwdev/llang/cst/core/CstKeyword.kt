package com.lhwdev.llang.cst.core

import com.lhwdev.llang.cst.CstNodeInfo
import com.lhwdev.llang.token.Token


class CstKeyword(token: Token) : CstLeafNode(token) {
	companion object Info : CstNodeInfo<CstKeyword> {
		/**
		 * This prevents nodes containing [CstKeyword] as an element to be created as a dummy
		 * element.
		 */
		override fun dummyNode() = null
	}
}
