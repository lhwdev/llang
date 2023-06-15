package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.core.*
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.node
import com.lhwdev.llang.parser.nullableNode
import com.lhwdev.llang.parser.nullableStructuredNode
import com.lhwdev.llang.parser.util.cstWsSeparatedListInline
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.*


fun CstParseContext.cstWs(): CstWs = node(CstWs) {
	cstWhitespaceOrNull() ?: cstLineBreakOrNull() ?: cstCommentOrNull() ?: discard()
}

fun CstParseContext.cstWsOrNull(): CstWs? = nullableNode(CstWs) {
	cstWhitespaceOrNull() ?: cstLineBreakOrNull() ?: cstCommentOrNull()
}

fun CstParseContext.cstWss(): CstWss = node(CstWss) {
	disableAdjacentImplicitNode() // maybe cause infinite recursion; cstWss is used by CstParseContext
	val nodes = mutableListOf<CstWs>()
	while(true) {
		val node = cstWsOrNull() ?: break
		nodes += node
	}
	
	CstWss(nodes)
}

fun CstParseContext.cstWssOrEmpty(): CstWss = node(CstWss) {
	CstWss(cstWsSeparatedListInline { cstWsOrNull() })
}


fun CstParseContext.cstWhitespaceOrNull(): CstWhitespace? =
	nullableStructuredNode(CstWhitespace) { code.parseWhitespace()?.let { CstWhitespace(it) } }


fun CstParseContext.cstLineBreakOrNull(): CstLineBreak? =
	nullableStructuredNode(CstLineBreak) { code.parseLineBreak()?.let { CstLineBreak(it) } }


fun CstParseContext.cstCommentOrNull(): CstComment? = nullableNode(CstComment) {
	val begin = code.parseCommentBeginOrNull()
	begin?.let { parseCstComment(CstComment.Begin(it)) }
}

private fun CstParseContext.parseCstComment(begin: CstComment.Begin): CstComment {
	val nodes = mutableListOf<CstWs>(begin)
	
	when(val kind = (begin.token.kind as TokenKinds.Comment.CommentBegin).kind) {
		TokenKinds.Comment.Eol -> {
			nodes += CstComment.Content(code.parseInEolComment())
		}
		
		is TokenKinds.Comment.BlockKind -> {
			while(true) {
				val token = code.parseInBlockComment(kind)
				when(token.kind as TokenKinds.Comment) {
					is TokenKinds.Comment.CommentBegin ->
						nodes += node(CstComment) { parseCstComment(CstComment.Begin(token)) }
					
					is TokenKinds.Comment.CommentEnd -> {
						nodes += CstComment.End(token)
						break
					}
					
					is TokenKinds.Comment.Content ->
						nodes += CstComment.Content(token)
				}
			}
		}
	}
	
	return CstComment(nodes)
}
