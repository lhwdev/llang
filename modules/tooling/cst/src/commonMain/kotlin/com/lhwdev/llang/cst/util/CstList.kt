package com.lhwdev.llang.cst.util

import com.lhwdev.llang.cst.*
import com.lhwdev.llang.token.TokenKinds


class CstList<Item : CstNode, Separator : CstNode>(
	c: CstParseContext,
	item: CstNodeFactory<Item>,
	separator: CstNodeFactory<Separator>,
	// allowTrailing: Boolean = true,
	// allowWhitespace: Boolean = true, // just 'ALLOW'
) : CstNode {
	val nodes: List<CstNode> = c.cstParseTokensList(item, separator)
	
	private fun CstParseContext.cstParseTokensList(
		item: CstNodeFactory<Item>,
		separator: CstNodeFactory<Separator>,
	): List<CstNode> {
		val nodes = mutableListOf<CstNode>()
		while(true) {
			val itemNode = nodeOrNull { node { item() } }
			if(itemNode != null) {
				nodes += itemNode
			} else {
				// if(!allowTrailing) {
				// 	// ???
				// }
				break
			}
			val separatorNode = nodeOrNull { node { separator() } }
			if(separatorNode != null) {
				nodes += separatorNode
			} else {
				break
			}
		}
		return nodes
	}
	
	fun itemAt(index: Int): Item {
		@Suppress("UNCHECKED_CAST")
		return nodes[index * 2] as Item
	}
	
	fun separatorAt(index: Int): Separator {
		@Suppress("UNCHECKED_CAST")
		return nodes[index * 2 + 1] as Separator
	}
	
	inline fun forEachItem(block: (Item) -> Unit) {
		for(i in nodes.indices step 2) {
			@Suppress("UNCHECKED_CAST")
			block(nodes[i] as Item)
		}
	}
}


fun <Item : CstNode> CstCommaList(c: CstParseContext, item: CstNodeFactory<Item>) =
	CstList(c, item = item, separator = { cstLeaf(TokenKinds.Operation.Other.Comma) })
