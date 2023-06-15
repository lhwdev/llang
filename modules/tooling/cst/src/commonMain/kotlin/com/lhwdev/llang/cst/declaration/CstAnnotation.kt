package com.lhwdev.llang.cst.declaration

import com.lhwdev.llang.cst.CstNodeInfo
import com.lhwdev.llang.cst.core.CstAccessTarget
import com.lhwdev.llang.cst.core.CstIdentifier
import com.lhwdev.llang.cst.expression.CstExpression
import com.lhwdev.llang.cst.expression.CstTuple
import com.lhwdev.llang.cst.util.CstWsSeparatedList


class CstAnnotations(val annotations: CstWsSeparatedList<CstAnnotation>)


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
	
	class Call(val name: CstAccessTarget, val params: CstTuple) : CstAnnotation()
	
	companion object Info : CstNodeInfo<CstAnnotation> {
		override fun dummyNode() = Name(CstIdentifier.dummyNode())
	}
}
