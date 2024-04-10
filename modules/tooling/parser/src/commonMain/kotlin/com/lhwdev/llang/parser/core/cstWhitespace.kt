package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.structure.core.*
import com.lhwdev.llang.parser.*
import com.lhwdev.llang.parsing.discard
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.*


fun CstParseContext.cstWs(): CstWs = node {
	cstWhitespaceOrNull() ?: cstLineBreakOrNull() ?: cstCommentOrNull() ?: discard()
}

fun CstParseContext.cstWsOrNull(): CstWs? = nullableNode {
	cstWhitespaceOrNull() ?: cstLineBreakOrNull() ?: cstCommentOrNull()
}

fun CstParseContext.cstWssNonEmpty(): CstWss = node {
	disableAdjacentImplicitNode() // maybe cause infinite recursion; cstWss is used by CstParseContext
	val nodes = mutableListOf<CstWs>()
	while(true) {
		val node = cstWsOrNull() ?: break
		nodes += node
	}
	
	if(nodes.isEmpty()) discard { "wss is not empty" }
	CstWss(nodes)
}

fun CstParseContext.cstWssOrEmpty(): CstWss = node {
	disableAdjacentImplicitNode() // maybe cause infinite recursion; cstWss is used by CstParseContext
	val nodes = mutableListOf<CstWs>()
	while(true) {
		val node = cstWsOrNull() ?: break
		nodes += node
	}
	
	CstWss(nodes)
}

fun CstParseContext.cstWssOrNull(): CstWss? = nullableNode {
	disableAdjacentImplicitNode() // maybe cause infinite recursion; cstWss is used by CstParseContext
	val nodes = mutableListOf<CstWs>()
	while(true) {
		val node = cstWsOrNull() ?: break
		nodes += node
	}
	
	if(nodes.isEmpty()) {
		null
	} else {
		CstWss(nodes)
	}
}


fun CstParseContext.cstWhitespaceOrNull(): CstWhitespace? =
	nullableLeafNode { code.parseWhitespace()?.let { CstWhitespace(it) } }


fun CstParseContext.cstLineBreakOrNull(): CstLineBreak? =
	nullableLeafNode { code.parseLineBreak()?.let { CstLineBreak(it) } }


fun CstParseContext.cstCommentOrNull(): CstComment? = nullableNode {
	val begin = nullableLeafNode {
		code.parseCommentBeginOrNull()
			?.let { CstComment.Begin(it) }
	}
	begin?.let { parseCstComment(it) }
}

private fun CstParseContext.parseCstComment(begin: CstComment.Begin): CstComment {
	val nodes = mutableListOf<CstWs>(begin)
	
	when(val kind = (begin.token.kind as TokenKinds.Comment.CommentBegin).kind) {
		TokenKinds.Comment.Eol -> {
			nodes += leafNode { CstComment.Content(code.parseInEolComment()) }
		}
		
		is TokenKinds.Comment.BlockKind -> {
			while(true) {
				val node = leafNode {
					val token = code.parseInBlockComment(kind)
					
					when(token.kind as TokenKinds.Comment) {
						is TokenKinds.Comment.CommentBegin -> {
							markCurrentAsDetached()
							CstComment.Begin(token)
						}
						
						is TokenKinds.Comment.CommentEnd -> CstComment.End(token)
						is TokenKinds.Comment.Content -> CstComment.Content(token)
					}
				}
				when(node) {
					is CstComment.Begin -> {
						nodes += parseCstComment(node)
					}
					
					is CstComment.End -> {
						nodes += node
						break
					}
					
					is CstComment.Content ->
						nodes += node
					
					else -> {}
				}
			}
		}
	}
	
	return CstComment(nodes)
}
