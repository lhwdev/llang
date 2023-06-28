package com.lhwdev.llang.cst.structure.declaration

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstIdentifier
import com.lhwdev.llang.cst.structure.core.CstLeafNodeImpl
import com.lhwdev.llang.cst.structure.core.CstModifiers
import com.lhwdev.llang.cst.structure.expression.CstExpression
import com.lhwdev.llang.cst.structure.type.CstType
import com.lhwdev.llang.cst.structure.util.CstOptional
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl


/**
 * `val` / `var` / `const`
 */
class CstVariableKind(token: Token) : CstLeafNodeImpl(token) {
	companion object Info : CstNodeInfo<CstVariableKind> {
		override fun dummyNode() = CstVariableKind(TokenImpl.dummyIllegal())
	}
}


sealed class CstVariable(
	final override val annotations: CstAnnotations,
	
	val context: CstOptional<CstContextDeclaration>,
	
	final override val modifiers: CstModifiers,
	
	val kind: CstVariableKind, // const/val/var
	
	val extensionReceiverParameter: CstOptional<CstExtensionReceiverParameter>,
	
	final override val name: CstIdentifier,
	
	val type: CstOptional<CstType>,
	
	val accessor: Accessor,
) : CstNamedDeclaration {
	sealed class Accessor : CstNode {
		companion object Info : CstNodeInfo<Accessor> {
			override fun dummyNode() = NoAccessor
		}
	}
	
	object NoAccessor : Accessor()
	
	class Delegation(val to: CstExpression) : Accessor() {
		companion object Info : CstNodeInfo<Delegation> {
			override fun dummyNode() = Delegation(CstExpression.dummyNode())
		}
	}
	
	class Normal(
		val initializer: CstOptional<CstExpression>,
		val accessors: CstDeclarations<CstAccessorFunction>,
	) : Accessor() {
		companion object Info : CstNodeInfo<Normal> {
			override fun dummyNode() = Normal(
				initializer = CstOptional.dummyNode(),
				accessors = CstDeclarations(emptyList()),
			)
		}
	}
}


class CstStandaloneVariable(
	annotations: CstAnnotations,
	context: CstOptional<CstContextDeclaration>,
	modifiers: CstModifiers,
	kind: CstVariableKind,
	extensionReceiverParameter: CstOptional<CstExtensionReceiverParameter>,
	name: CstIdentifier,
	type: CstOptional<CstType>,
	accessor: Accessor,
) : CstVariable(
	annotations,
	context,
	modifiers,
	kind,
	extensionReceiverParameter,
	name,
	type,
	accessor,
),
	CstStandaloneDeclaration {
	companion object Info : CstNodeInfo<CstStandaloneVariable> {
		override fun dummyNode() = CstStandaloneVariable(
			annotations = CstAnnotations.dummyNode(),
			context = CstOptional.dummyNode(),
			modifiers = CstModifiers.dummyNode(),
			kind = CstVariableKind.dummyNode(),
			extensionReceiverParameter = CstOptional.dummyNode(),
			name = CstIdentifier.dummyNode(),
			type = CstOptional.dummyNode(),
			accessor = NoAccessor,
		)
	}
}

class CstLocalVariable(
	annotations: CstAnnotations,
	context: CstOptional<CstContextDeclaration>,
	modifiers: CstModifiers,
	kind: CstVariableKind,
	extensionReceiverParameter: CstOptional<CstExtensionReceiverParameter>,
	name: CstIdentifier,
	type: CstOptional<CstType>,
	accessor: Accessor,
) : CstVariable(
	annotations,
	context,
	modifiers,
	kind,
	extensionReceiverParameter,
	name,
	type,
	accessor,
), CstLocalDeclaration

class CstMemberVariable(
	modifiers: CstModifiers,
	annotations: CstAnnotations,
	context: CstOptional<CstContextDeclaration>,
	kind: CstVariableKind,
	extensionReceiverParameter: CstOptional<CstExtensionReceiverParameter>,
	name: CstIdentifier,
	type: CstOptional<CstType>,
	accessor: Accessor,
) : CstVariable(
	annotations,
	context,
	modifiers,
	kind,
	extensionReceiverParameter,
	name,
	type,
	accessor,
), CstMemberDeclaration
