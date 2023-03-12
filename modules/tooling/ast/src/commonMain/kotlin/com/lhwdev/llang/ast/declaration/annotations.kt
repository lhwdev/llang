package com.lhwdev.llang.ast.declaration


/**
 * All [AstBuiltinColor]s will be folded into `explicitColors/implicitColors` in fir.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class AstBuiltinColor

/**
 * For all inferable properties, like returnType of block/function, type of variable,
 * valueParameter type/isInline/isSuspend etc. for lambda.
 *
 * For example: [AstTypeReference][com.lhwdev.llang.ast.reference.AstTypeReference] that can be inferred.
 * Variables with this annotation are declared as `Inferable<AstTypeReference>`.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class AstInferable
