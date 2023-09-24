package com.lhwdev.llang.tokenizer

import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.source.*


/**
 * Even though this parses in the context of expression, this should recognize all the operators.
 */
fun CodeSource.parseOperationInAnyExpression(): Token {
	val next = peek()
	
	if(CharacterKind.isLetter(current)) token {
		advanceIdentifier()
		when(currentSpan) {
			"in" -> TokenKinds.Operator.Expression.In
			"is" -> TokenKinds.Operator.Expression.Is
			"as" -> if(matchesAdvance('?')) {
				TokenKinds.Operator.Expression.AsOrNull
			} else {
				TokenKinds.Operator.Expression.As
			}
			
			// Infix operator is handled by cst level; not token level.
			else -> TokenKinds.Identifier.Simple
		}
	}
	
	return when(current) {
		// Infix operator is handled by cst level; not token level.
		'`' -> token(TokenKinds.Identifier.Quoted) {
			advanceIdentifier()
		}
		
		'+' -> when(next) {
			'=' -> token(TokenKinds.Operator.Assign.PlusAssign, length = 2)
			else -> token(TokenKinds.Operator.Arithmetic.Plus)
		}
		
		'-' -> when(next) {
			'=' -> token(TokenKinds.Operator.Assign.MinusAssign, length = 2)
			'>' -> token(TokenKinds.Operator.Other.ArrowRight, length = 2)
			else -> token(TokenKinds.Operator.Arithmetic.Minus)
		}
		
		'*' -> token(TokenKinds.Operator.Arithmetic.Times)
		
		'/' -> token(TokenKinds.Operator.Arithmetic.Divide)
		
		'%' -> token(TokenKinds.Operator.Arithmetic.Remainder)
		
		'=' -> when(next) {
			'=' -> when(peek(2)) {
				'=' -> token(TokenKinds.Operator.Compare.IdentityEquals, length = 3)
				else -> token(TokenKinds.Operator.Compare.Equals, length = 2)
			}
			
			else -> token(TokenKinds.Operator.Assign.Assign, length = 1)
		}
		
		'!' -> when(next) {
			'=' -> when(peek(2)) {
				'=' -> token(TokenKinds.Operator.Compare.NotIdentityEquals, length = 3)
				else -> token(TokenKinds.Operator.Compare.NotEquals, length = 2)
			}
			
			else -> if(CharacterKind.isLetter(current)) token {
				advanceIdentifier()
				when(currentSpan) {
					"is" -> TokenKinds.Operator.Expression.NotIs
					"in" -> TokenKinds.Operator.Expression.NotIn
					
					else -> TokenKinds.Illegal
					// 'not infix' operator(ex: true !or false)
					// -> just... use... 'nor'... not '!or'...
					// else -> TokenKinds.Identifier.Simple
				}
			} else token(TokenKinds.Operator.Logic.Not)
		}
		
		'<' -> when(next) {
			'=' -> token(TokenKinds.Operator.Compare.LtEq, length = 2)
			'.' -> when(peek(2)) {
				'.' -> when(peek(3)) {
					'<' -> token(TokenKinds.Operator.Expression.RangeAfterUntil, 4)
					else -> token(TokenKinds.Operator.Expression.RangeAfterTo, 3)
				}
				
				else -> illegalToken(2)
			}
			
			else -> token(TokenKinds.Operator.Compare.Lt)
		}
		
		'>' -> when(next) {
			'=' -> token(TokenKinds.Operator.Compare.GtEq, length = 2)
			else -> token(TokenKinds.Operator.Compare.Gt)
		}
		
		'(' -> token(TokenKinds.Operator.Group.LeftParen)
		')' -> token(TokenKinds.Operator.Group.RightParen)
		'[' -> token(TokenKinds.Operator.Group.LeftSquareBracket)
		']' -> token(TokenKinds.Operator.Group.RightSquareBracket)
		'{' -> token(TokenKinds.Operator.Group.LeftBrace)
		'}' -> token(TokenKinds.Operator.Group.RightBrace)
		
		'&' -> when(next) {
			'&' -> token(TokenKinds.Operator.Logic.And, length = 2)
			else -> illegalToken()
		}
		
		'|' -> when(next) {
			'|' -> token(TokenKinds.Operator.Logic.Or, length = 2)
			else -> illegalToken()
		}
		
		'.' -> @Suppress("IntroduceWhenSubject") when { // TODO: limit on adjacent tokens
			next == '.' -> when(peek(2)) {
				'<' -> token(TokenKinds.Operator.Expression.RangeUntil, length = 3)
				'.' -> token(TokenKinds.Operator.Other.Etc, length = 3)
				else -> token(TokenKinds.Operator.Expression.RangeTo, length = 2)
			}
			
			else -> token(TokenKinds.Operator.Access.Dot)
		}
		
		',' -> token(TokenKinds.Operator.Other.Comma)
		
		'?' -> when(next) {
			':' -> token(TokenKinds.Operator.Expression.Elvis, length = 2)
			else -> token(TokenKinds.Operator.Other.PropagateError)
		}
		
		':' -> when(next) {
			':' -> token(TokenKinds.Operator.Access.Metadata)
			else -> token(TokenKinds.Operator.Other.Colon)
		}
		
		'#' -> token(TokenKinds.Operator.Other.AnnotationMarker)
		
		else -> illegalToken()
	}
}
