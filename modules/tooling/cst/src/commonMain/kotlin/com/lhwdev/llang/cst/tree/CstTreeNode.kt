package com.lhwdev.llang.cst.tree

import com.lhwdev.llang.token.Token


interface CstTreeNode {
	val parent: CstTreeNode
	
	
	val isAttached: Boolean
	
	val isRead: Boolean
	
	val isLeaf: Boolean
	
	
	/**
	 * Points to `this` if no other external source exists.
	 */
	val source: CstTreeNode
	
	
	val tokens: List<Token>
	
    val children: List<CstTreeNode>
}
