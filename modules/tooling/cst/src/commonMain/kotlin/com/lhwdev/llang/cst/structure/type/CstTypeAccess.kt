package com.lhwdev.llang.cst.structure.type

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeImpl
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstIdentifier


sealed interface CstConcreteType : CstNode {
	override val info: CstNodeInfo<out CstConcreteType>
		get() = Info
	
	companion object Info : CstNodeInfo<CstConcreteType> {
		override fun dummyNode() = CstTypeIdentifier.dummyNode()
	}
}

class CstTypeIdentifier(val identifier: CstIdentifier) : CstConcreteType, CstNodeImpl() {
	override val info
		get() = Info
	
	companion object Info : CstNodeInfo<CstTypeIdentifier> {
		override fun dummyNode() = CstTypeIdentifier(CstIdentifier.dummyNode())
	}
}

/**
 * Parsed by Expression Parser
 *
 * ## Examples
 * - `com.lhwdev.Hello` is parsed as `CstAccess(CstAccess(CstIdentifier("com"), CstIdentifier("lhwdev")), CstIdentifier("Hello"))`
 * - type `MyClass<Type>.Inner` is parsed as `CstAccess(CstTypeCall(CstIdentifier("MyClass"), type), CstIdentifier("Inner"))`
 */
class CstTypeAccess(
	val parent: CstConcreteType,
	val item: CstTypeIdentifier,
) : CstNode, CstConcreteType, CstNodeImpl() {
	override val info
		get() = Info
	
	companion object Info : CstNodeInfo<CstTypeAccess> {
		override fun dummyNode() = CstTypeAccess(
			CstTypeIdentifier.dummyNode(),
			CstTypeIdentifier.dummyNode(),
		)
		
	}
}
