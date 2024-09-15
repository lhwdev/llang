package com.lhwdev.llang.test.parser.node

import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.CstParserNode
import com.lhwdev.llang.cst.tree.CstTreeNode
import com.lhwdev.llang.token.Token


class CstIntermediateNode : CstParserNode {

    override val kind: CstParseContext.NodeKind = TODO("SET VALUE")

    override val parent: CstTreeNode = TODO("SET VALUE")

    override val isAttached: Boolean = TODO("SET VALUE")

    override val isRead: Boolean = TODO("SET VALUE")

    override val source: CstTreeNode = TODO("SET VALUE")

    override val tokens: List<Token> = TODO("SET VALUE")

    override val children: List<CstTreeNode> = TODO("SET VALUE")
	
}
