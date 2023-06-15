package com.lhwdev.llang.cst.structure.core

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.token.TokenImpl


sealed interface CstAccessTarget : CstNode


/**
 * Parsed by Expression Parser
 *
 * ## Examples
 * - `com.lhwdev.Hello` is parsed as `CstAccess(CstAccess(CstIdentifier("com"), CstIdentifier("lhwdev")), CstIdentifier("Hello"))`
 * - type `MyClass<Type>.Inner` is parsed as `CstAccess(CstTypeCall(CstIdentifier("MyClass"), type), CstIdentifier("Inner"))`
 */
class CstAccess(
	val parent: CstAccessTarget,
	val item: CstIdentifier,
) : CstNode, CstAccessTarget {
	companion object Info : CstNodeInfo<CstAccess> {
		override fun dummyNode() = CstAccess(
			CstIdentifier(TokenImpl.dummyIllegal()),
			CstIdentifier(TokenImpl.dummyIllegal())
		)
		
	}
}
