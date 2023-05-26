package com.lhwdev.llang.cst.declaration

import com.lhwdev.llang.cst.CstTokenWs
import com.lhwdev.llang.cst.CstTokens


class CstClass(
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
)
