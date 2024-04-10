package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.parser.CstParseContext


/**
 * Note: implicit node must be a single node. If this contains multiple nodes, wrap with `node { }`.
 */
fun CstParseContext.cstImplicitNodeOrNull(): CstNode? = cstWssOrNull()
