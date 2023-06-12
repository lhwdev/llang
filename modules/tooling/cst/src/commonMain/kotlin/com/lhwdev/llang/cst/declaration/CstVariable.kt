package com.lhwdev.llang.cst.declaration

import com.lhwdev.llang.cst.core.CstIdentifier
import com.lhwdev.llang.cst.core.CstLeafNode
import com.lhwdev.llang.cst.core.CstModifiers
import com.lhwdev.llang.cst.type.CstType
import com.lhwdev.llang.cst.util.CstOptional
import com.lhwdev.llang.token.Token


/**
 * `val` / `var` / `const`
 */
class CstVariableKind(token: Token) : CstLeafNode(token)


sealed class CstVariable(
	val modifiers: CstModifiers, // context/(expect/actual)
	val kind: CstVariableKind, // const/val/var
	override val name: CstIdentifier,
	val type: CstOptional<CstType>,
) : CstDeclaration


class CstStandaloneVariable(
	modifiers: CstModifiers,
	kind: CstVariableKind,
	name: CstIdentifier,
) : CstVariable(name), CstAccessibleDeclaration

class CstLocalVariable(
	name: CstIdentifier,
) : CstVariable(name)

class CstMemberVariable(
	override val modality: CstModality, // final/open/abstract
	kind: CstVariableKind,
	name: CstIdentifier,
) : CstVariable(name), CstMemberDeclaration
