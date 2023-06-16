package com.lhwdev.llang.cst.structure.component

import com.lhwdev.llang.cst.structure.CstNode


/**
 * Very useful for kleene-structure of language plugin (IDE/compiler plugin).
 * Any structure extending [CstComponent] will be specially handled during node transformation,
 * such as 'CstNode -> AstNode transformation'. You can make your own `AstComponent` then make
 * your [CstComponent] return that `AstComponent`.
 */
interface CstComponent : CstNode
