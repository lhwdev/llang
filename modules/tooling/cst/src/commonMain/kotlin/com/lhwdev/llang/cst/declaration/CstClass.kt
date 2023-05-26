package com.lhwdev.llang.cst.declaration

import com.lhwdev.llang.cst.CstCreateContext
import com.lhwdev.llang.token.TokenKinds


class CstClass(c: CstCreateContext) : CstDeclaration(c) {
	override val annotations = c.cstAnnotation()
	
	override val modifiers = c.cstModifiers {
		// public, private, protected, internal
		// open, final
		// value
		// expect, actual, external
		// abstract
		listOf(Visibility, Modality, IsValue, BodyOmission, Abstract)
	}
	
	val partialClassKind = c.oneOf {
		tokenCase(TokenKinds.Keyword.)
	}
}


fun CstCreateContext.cstClass(): CstClass =
	declaration { CstClass(this) }


/*

	val prefixWhitespaces: CstTokenWs,
	
	val annotations: CstTokens<CstAnnotation>,
	val visibility: CstTokens.Single<CstVisibility>,
	val modality: CstTokens.Single<CstModality>,
	val isValue: CstTokens.Single<>,
	val bodyOmission: CstTokens.Single<CstBodyOmissionKind?>,
	
	val classKind: CstTokens.Single<CstClassKind>,
	val name: CstTokens.Single<CstName>,
	val typeParameters: CstTokens.Multiple<CstTypeParameter>,
	val primaryConstructor: CstTokens.Multiple<CstPrimaryConstructor?>,
	
	val postfixWhitespaces: CstTokenWs,
 */
