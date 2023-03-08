package com.lhwdev.llang.ast.declaration

import com.lhwdev.llang.ast.expression.AstExpression
import com.lhwdev.llang.ast.reference.AstClassReference
import com.lhwdev.llang.common.BodyOmissionKind
import com.lhwdev.llang.common.ClassKind
import com.lhwdev.llang.common.Modality
import com.lhwdev.llang.common.Visibility


/**
 * ```
 * [HelloWorld]
 * public open class HelloWorld<T> [hello] constructor(...) : A(), B where T : Super {
 *
 * }
 * ```
 */
interface AstClass :
	AstCodeDeclaration,
	AstTopLevelDeclaration, AstEnclosedDeclaration, AstLocalDeclaration,
	AstDeclarationContainer {
	
	override val annotations: List<AstAnnotation>
	
	override val visibility: Visibility
	
	val modality: Modality
	
	val isValue: Boolean
	
	override val bodyOmission: BodyOmissionKind?
	
	val classKind: ClassKind
	
	override val name: String
	
	val typeParameters: List<AstTypeParameter>
	
	val primaryConstructor: AstConstructor
	
	val supers: List<AstClassSuper>
	
	override val children: List<AstDeclaration>
}


interface AstClassSuper {
	val reference: AstClassReference
	
	val delegation: AstExpression?
}
