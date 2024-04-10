package com.lhwdev.llang.parser.expression

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.core.CstLeafNodeImpl
import com.lhwdev.llang.cst.structure.expression.CstConstLiteral
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.core.cstIdentifier
import com.lhwdev.llang.parser.core.cstLeafNode
import com.lhwdev.llang.parser.leafNode
import com.lhwdev.llang.parser.node
import com.lhwdev.llang.parser.structuredNode
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.parseInStringLiteral
import com.lhwdev.llang.tokenizer.parseStringLiteralBegin
import com.lhwdev.llang.tokenizer.parseStringLiteralEnd


fun CstParseContext.cstConstLiteral(): CstConstLiteral = leafNode {
	TODO()
}


fun CstParseContext.cstStringLiteral(): CstConstLiteral.String = node {
	val begin = cstStringLiteralBegin()
	val list = mutableListOf<CstConstLiteral.String.Element>(begin)
	
	while(true) {
		when(val content = cstContentInStringLiteral(begin.quote)) {
			is CstConstLiteral.String.Content -> {
				list += content
			}
			
			is CstConstLiteral.String.End -> {
				list += content
				break
			}
			
			is CstLeafNodeImpl -> when(content.token.kind) {
				TokenKinds.StringLiteral.TemplateVariable -> {
					list += structuredNode {
						insertChildNode(content)
						val name = cstIdentifier()
						
						CstConstLiteral.String.TemplateVariable(name)
					}
				}
				
				TokenKinds.StringLiteral.TemplateExpression -> {
					list += structuredNode {
						insertChildNode(content)
						val expression = cstExpression()
						cstLeafNode(TokenKinds.Operator.Group.RightBrace, "}")
						
						CstConstLiteral.String.TemplateExpression(expression)
					}
				}
			}
		}
	}
	
	CstConstLiteral.String(list)
}

private fun CstParseContext.cstStringLiteralBegin(): CstConstLiteral.String.Begin =
	leafNode { CstConstLiteral.String.Begin(code.parseStringLiteralBegin()) }

private fun CstParseContext.cstStringLiteralEnd(): CstConstLiteral.String.End =
	leafNode { CstConstLiteral.String.End(code.parseStringLiteralEnd()) }

private fun CstParseContext.cstContentInStringLiteral(
	quote: TokenKinds.StringLiteral.Quote,
): CstNode = leafNode {
	val token = code.parseInStringLiteral(quote)
	when(token.kind) {
		TokenKinds.StringLiteral.TemplateExpression -> {
			markCurrentAsDetached()
			CstLeafNodeImpl(token)
		}
		
		TokenKinds.StringLiteral.TemplateVariable -> {
			markCurrentAsDetached()
			CstLeafNodeImpl(token)
		}
		
		is TokenKinds.StringLiteral.QuoteEnd -> CstConstLiteral.String.End(token)
		else -> CstConstLiteral.String.Content(token)
	}
}
