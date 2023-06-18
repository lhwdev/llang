package com.lhwdev.llang.cst.structure.type

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstIdentifier
import com.lhwdev.llang.token.TokenImpl


sealed interface CstTypeAccessTarget : CstNode


/**
 * Parsed by Expression Parser
 *
 * ## Examples
 * - `com.lhwdev.Hello` is parsed as `CstAccess(CstAccess(CstIdentifier("com"), CstIdentifier("lhwdev")), CstIdentifier("Hello"))`
 * - type `MyClass<Type>.Inner` is parsed as `CstAccess(CstTypeCall(CstIdentifier("MyClass"), type), CstIdentifier("Inner"))`
 */
class CstTypeAccess(
	val parent: CstTypeAccessTarget,
	val item: CstIdentifier,
) : CstNode, CstTypeAccessTarget {
	companion object Info : CstNodeInfo<CstTypeAccess> {
		override fun dummyNode() = CstTypeAccess(
			CstIdentifier(TokenImpl.dummyIllegal()),
			CstIdentifier(TokenImpl.dummyIllegal())
		)
		
	}
}
