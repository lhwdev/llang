package com.lhwdev.llang.cst.declaration

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.cst.core.CstIdentifier
import com.lhwdev.llang.cst.core.CstLeafNode
import com.lhwdev.llang.cst.core.CstModifiers
import com.lhwdev.llang.cst.expression.CstExpression
import com.lhwdev.llang.cst.type.CstType
import com.lhwdev.llang.cst.util.CstOptional
import com.lhwdev.llang.token.Token


/**
 * `val` / `var` / `const`
 */
class CstVariableKind(token: Token) : CstLeafNode(token)


sealed class CstVariable(
	override val modifiers: CstModifiers, // context/(expect/actual)
	val kind: CstVariableKind, // const/val/var
	override val name: CstIdentifier,
	val type: CstOptional<CstType>,
	val accessor: Accessor,
) : CstDeclaration {
	sealed class Accessor : CstNode
	
	class Delegation(val to: CstExpression) : Accessor()
	
	class Normal(
		val initializer: CstOptional<CstExpression>,
		val getter: CstOptional<CstGetter>,
		val setter: CstOptional<CstSetter>,
	) : Accessor()
}


class CstStandaloneVariable(
	modifiers: CstModifiers,
	kind: CstVariableKind,
	name: CstIdentifier,
	type: CstOptional<CstType>,
	accessor: Accessor,
) : CstVariable(modifiers, kind, name, type, accessor), CstAccessibleDeclaration

class CstLocalVariable(
	modifiers: CstModifiers,
	kind: CstVariableKind,
	name: CstIdentifier,
	type: CstOptional<CstType>,
	accessor: Accessor,
) : CstVariable(modifiers, kind, name, type, accessor), CstLocalDeclaration

class CstMemberVariable(
	modifiers: CstModifiers,
	kind: CstVariableKind,
	name: CstIdentifier,
	type: CstOptional<CstType>,
	accessor: Accessor,
) : CstVariable(modifiers, kind, name, type, accessor), CstMemberDeclaration
