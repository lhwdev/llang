package com.lhwdev.llang.cst.declaration

import com.lhwdev.llang.cst.CstNodeInfo
import com.lhwdev.llang.cst.CstParseContext
import com.lhwdev.llang.token.TokenKinds


class CstClass(c: CstParseContext) : CstDeclaration(c) {
	companion object Info : CstNodeInfo<CstClass>
	
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


fun CstParseContext.cstClass(): CstClass =
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
