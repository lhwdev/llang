package com.lhwdev.llang.cst.declaration

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.CstNodeInfo
import com.lhwdev.llang.cst.core.CstIdentifier
import com.lhwdev.llang.cst.core.CstLeafNode
import com.lhwdev.llang.cst.core.CstModifiers
import com.lhwdev.llang.cst.expression.CstExpression
import com.lhwdev.llang.cst.type.CstType
import com.lhwdev.llang.cst.util.CstOptional
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl


/**
 * `val` / `var` / `const`
 */
class CstVariableKind(token: Token) : CstLeafNode(token) {
	companion object Info : CstNodeInfo<CstVariableKind> {
		override fun dummyNode() = CstVariableKind(TokenImpl.dummyIllegal())
	}
}


sealed class CstVariable(
	override val annotations: CstAnnotations,
	val context: CstContextDeclaration,
	override val modifiers: CstModifiers,
	val kind: CstVariableKind, // const/val/var
	override val name: CstIdentifier,
	val type: CstOptional<CstType>,
	val accessor: Accessor,
) : CstDeclaration {
	sealed class Accessor : CstNode {
		companion object Info : CstNodeInfo<Accessor> {
			override fun dummyNode() = NoAccessor
		}
	}
	
	object NoAccessor : Accessor()
	
	class Delegation(val to: CstExpression) : Accessor()
	
	class Normal(
		val initializer: CstOptional<CstExpression>,
		val getter: CstOptional<CstGetter>,
		val setter: CstOptional<CstSetter>,
	) : Accessor()
}


class CstStandaloneVariable(
	annotations: CstAnnotations,
	context: CstContextDeclaration,
	modifiers: CstModifiers,
	kind: CstVariableKind,
	name: CstIdentifier,
	type: CstOptional<CstType>,
	accessor: Accessor,
) : CstVariable(annotations, context, modifiers, kind, name, type, accessor),
	CstStandaloneDeclaration {
	companion object Info : CstNodeInfo<CstStandaloneVariable> {
		override fun dummyNode() = CstStandaloneVariable(
			annotations = CstAnnotations.dummyNode(),
			context = CstContextDeclaration.dummyNode(),
			modifiers = CstModifiers.dummyNode(),
			kind = CstVariableKind.dummyNode(),
			name = CstIdentifier.dummyNode(),
			type = CstOptional.dummyNode(),
			accessor = NoAccessor,
		)
	}
}

class CstLocalVariable(
	annotations: CstAnnotations,
	context: CstContextDeclaration,
	modifiers: CstModifiers,
	kind: CstVariableKind,
	name: CstIdentifier,
	type: CstOptional<CstType>,
	accessor: Accessor,
) : CstVariable(annotations, context, modifiers, kind, name, type, accessor), CstLocalDeclaration

class CstMemberVariable(
	modifiers: CstModifiers,
	annotations: CstAnnotations,
	context: CstContextDeclaration,
	kind: CstVariableKind,
	name: CstIdentifier,
	type: CstOptional<CstType>,
	accessor: Accessor,
) : CstVariable(annotations, context, modifiers, kind, name, type, accessor), CstMemberDeclaration
