package com.lhwdev.llang.cst.structure.expression


sealed class CstFunctionCall<Argument : CstExpression>(
	val function: CstExpression,
	val argument: Argument,
) : CstExpression {
	class Call(function: CstExpression, arguments: CstTuple) :
		CstFunctionCall<CstTuple>(function, arguments)
	
	class Get(function: CstExpression, arguments: CstTuple) :
		CstFunctionCall<CstTuple>(function, arguments)
	
	class WithLambda(function: CstExpression, argument: CstLambdaExpression) :
		CstFunctionCall<CstLambdaExpression>(function, argument)
}
