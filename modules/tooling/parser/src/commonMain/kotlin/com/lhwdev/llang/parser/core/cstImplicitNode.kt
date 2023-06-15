package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.CstNode
import com.lhwdev.llang.parser.CstParseContext


@Suppress("RedundantNullableReturnType")
fun CstParseContext.cstImplicitNodeOrNull(): CstNode? = cstWss()
