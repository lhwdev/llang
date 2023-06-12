package com.lhwdev.llang.cst.core

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.CstNodeInfo
import com.lhwdev.llang.token.TokenImpl
import com.lhwdev.llang.token.TokenKinds


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
			CstIdentifier(TokenImpl.dummy(TokenKinds.Illegal, "")),
			CstIdentifier(TokenImpl.dummy(TokenKinds.Illegal, ""))
		)
		
	}
}
