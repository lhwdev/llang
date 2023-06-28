package com.lhwdev.llang.cst.structure.expression


sealed class CstFunctionCall<Argument : CstExpression>(
	val function: CstExpression,
	val argument: Argument,
) : CstExpression {
	abstract val arguments: CstTuple
	
	class Call(function: CstExpression, arguments: CstTuple) :
		CstFunctionCall<CstTuple>(function, arguments) {
		override val arguments: CstTuple get() = argument
	}
	
	class Get(function: CstExpression, arguments: CstTuple) :
		CstFunctionCall<CstTuple>(function, arguments) {
		override val arguments: CstTuple get() = argument
	}
	
	
	class InfixCall(function: CstExpression, argument: CstExpression) :
		CstFunctionCall<CstExpression>(function, argument) {
		override val arguments: CstTuple get() = CstTuple(listOf(argument))
	}
	
	
	class WithLambda(function: CstExpression, argument: CstLambdaExpression) :
		CstFunctionCall<CstLambdaExpression>(function, argument) {
		override val arguments: CstTuple get() = CstTuple(listOf(argument))
	}
	
}
