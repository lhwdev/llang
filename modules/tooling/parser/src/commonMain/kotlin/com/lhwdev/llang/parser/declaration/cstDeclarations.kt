package com.lhwdev.llang.parser.declaration

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.util.CstParsingContainerContext


// Note: this function includes `prefixWss` into CstRawNodeTree of CstDeclaration.
//   `prefixWss` includes last ldoc or comment. Follows heuristic of Intellij.
//   (try control/command+shift+up/down!)
//   - if ldoc exists, one ldoc block and everything between ldoc & declarations is included
//   - if not, one herd of comment is included; all series of eol/block comments without empty lines
//     between.
fun <Node : CstNode> CstParseContext.cstDeclarations(
	block: CstParsingContainerContext<CstNode>.() -> Node,
): Node {
	TODO()
}
