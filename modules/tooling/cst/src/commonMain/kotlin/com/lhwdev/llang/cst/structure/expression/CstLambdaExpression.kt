package com.lhwdev.llang.cst.structure.expression

import com.lhwdev.llang.cst.structure.CstNodeImpl
import com.lhwdev.llang.cst.structure.declaration.CstValueParameters
import com.lhwdev.llang.cst.structure.statement.CstStatements


class CstLambdaExpression(
	val valueParameters: CstValueParameters,
	val body: CstStatements,
) : CstExpression, CstNodeImpl()
