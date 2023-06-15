package com.lhwdev.llang.cst.structure.declaration

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstAccessTarget
import com.lhwdev.llang.cst.structure.core.CstIdentifier
import com.lhwdev.llang.cst.structure.expression.CstExpression
import com.lhwdev.llang.cst.structure.expression.CstTuple


class CstAnnotations(val annotations: List<CstAnnotation>) : CstNode {
	companion object Info : CstNodeInfo<CstAnnotations> {
		override fun dummyNode() = CstAnnotations(emptyList())
	}
}


sealed class CstAnnotation : CstDeclarationLike {
	/**
	 * Like `[hello]`
	 */
	class Name(val name: CstAccessTarget) : CstAnnotation()
	
	/**
	 * Like `[myProperty = true]` or `[user.hello(3) = 123]`
	 */
	class Property(
		val name: /* ???; just stub */ CstIdentifier,
		val value: CstExpression,
	) : CstAnnotation()
	
	/**
	 * Like `[hello(123, "ho", myFunction)]`
	 */
	class Call(val name: CstAccessTarget, val params: CstTuple) : CstAnnotation()
	
	companion object Info : CstNodeInfo<CstAnnotation> {
		override fun dummyNode() = Name(CstIdentifier.dummyNode())
	}
}
