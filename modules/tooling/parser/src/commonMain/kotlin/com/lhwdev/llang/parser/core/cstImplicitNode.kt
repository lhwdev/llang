package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.parser.CstParseContext


fun CstParseContext.cstImplicitNodeOrNull(): CstNode? = cstWssOrNull()
