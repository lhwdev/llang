package com.lhwdev.llang.parser.core

import com.lhwdev.llang.cst.core.CstComment
import com.lhwdev.llang.cst.core.CstLineBreak
import com.lhwdev.llang.cst.core.CstWhitespace
import com.lhwdev.llang.cst.core.CstWs
import com.lhwdev.llang.parser.*
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.*


val cstWs = CstNodeFactory { cstWs() }


fun CstParseContext.cstWs(): CstWs = node(CstWs) {
	cstWhitespaceOrNull() ?: cstLineBreakOrNull() ?: cstCommentOrNull() ?: discard()
}

fun CstParseContext.cstWsOrNull(): CstWs? = nullableNode(CstWs) {
	cstWhitespaceOrNull() ?: cstLineBreakOrNull() ?: cstCommentOrNull()
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
