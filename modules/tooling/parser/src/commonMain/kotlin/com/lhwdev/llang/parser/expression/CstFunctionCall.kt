package com.lhwdev.llang.parser.expression

import com.lhwdev.llang.cst.structure.expression.CstExpression
import com.lhwdev.llang.cst.structure.expression.CstTuple


class CstFunctionCall(val function: CstExpression, val arguments: CstTuple) : CstExpression
