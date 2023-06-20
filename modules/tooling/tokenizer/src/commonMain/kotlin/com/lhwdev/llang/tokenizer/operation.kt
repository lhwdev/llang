package com.lhwdev.llang.tokenizer

import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.source.*


/**
 * Even though this parses in the context of expression, this should recognize all the operators.
 * (except for eagerly parsed operations which are handled already)
 */
fun CodeSource.parseOperationInAnyExpression(): Token {
	val next = peek()
	
	if(CharacterKind.isLetter(current)) token {
		advanceIdentifier()
		when(currentSpan) {
			"in" -> TokenKinds.Operation.Expression.In
			"is" -> TokenKinds.Operation.Expression.Is
			"as" -> if(matchesAdvance('?')) {
				TokenKinds.Operation.Expression.AsOrNull
			} else {
				TokenKinds.Operation.Expression.As
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
			'=' -> token(TokenKinds.Operation.Assign.PlusAssign, length = 2)
			else -> token(TokenKinds.Operation.Arithmetic.Plus)
		}
		
		'-' -> when(next) {
			'=' -> token(TokenKinds.Operation.Assign.MinusAssign, length = 2)
			'>' -> token(TokenKinds.Operation.Other.ArrowRight, length = 2)
			else -> token(TokenKinds.Operation.Arithmetic.Minus)
		}
		
		'*' -> token(TokenKinds.Operation.Arithmetic.Times)
		
		'/' -> token(TokenKinds.Operation.Arithmetic.Divide)
		
		'%' -> token(TokenKinds.Operation.Arithmetic.Remainder)
		
		'=' -> when(next) {
			'=' -> when(peek(2)) {
				'=' -> token(TokenKinds.Operation.Compare.IdentityEquals, length = 3)
				else -> token(TokenKinds.Operation.Compare.Equals, length = 2)
			}
			
			else -> token(TokenKinds.Operation.Assign.Assign, length = 1)
		}
		
		'!' -> when(next) {
			'=' -> when(peek(2)) {
				'=' -> token(TokenKinds.Operation.Compare.NotIdentityEquals, length = 3)
				else -> token(TokenKinds.Operation.Compare.NotEquals, length = 2)
			}
			
			else -> if(CharacterKind.isLetter(current)) token {
				advanceIdentifier()
				when(currentSpan) {
					"is" -> TokenKinds.Operation.Expression.NotIs
					"in" -> TokenKinds.Operation.Expression.NotIn
					
					else -> TokenKinds.Illegal
					// 'not infix' operator(ex: true !or false)
					// -> just... use... 'nor'... not '!or'...
					// else -> TokenKinds.Identifier.Simple
				}
			} else token(TokenKinds.Operation.Logic.Not)
		}
		
		'<' -> when(next) {
			'=' -> token(TokenKinds.Operation.Compare.LtEq, length = 2)
			'.' -> when(peek(2)) {
				'.' -> when(peek(3)) {
					'<' -> token(TokenKinds.Operation.Expression.RangeAfterUntil, 4)
					else -> token(TokenKinds.Operation.Expression.RangeAfterTo, 3)
				}
				
				else -> illegalToken(2)
			}
			
			else -> token(TokenKinds.Operation.Compare.Lt)
		}
		
		'>' -> when(next) {
			'=' -> token(TokenKinds.Operation.Compare.GtEq, length = 2)
			else -> token(TokenKinds.Operation.Compare.Gt)
		}
		
		'&' -> when(next) {
			'&' -> token(TokenKinds.Operation.Logic.And, length = 2)
			else -> illegalToken()
		}
		
		'|' -> when(next) {
			'|' -> token(TokenKinds.Operation.Logic.Or, length = 2)
			else -> illegalToken()
		}
		
		'.' -> @Suppress("IntroduceWhenSubject") when { // TODO: limit on adjacent tokens
			next == '.' -> when(peek(2)) {
				'<' -> token(TokenKinds.Operation.Expression.RangeUntil, length = 3)
				'.' -> token(TokenKinds.Operation.Other.Etc, length = 3)
				else -> token(TokenKinds.Operation.Expression.RangeTo, length = 2)
			}
			
			else -> illegalToken()
		}
		
		'?' -> when(next) {
			':' -> token(TokenKinds.Operation.Expression.Elvis, length = 2)
			else -> token(TokenKinds.Operation.Other.PropagateError)
		}
		
		':' -> token(TokenKinds.Operation.Other.Colon)
		
		'#' -> token(TokenKinds.Operation.Other.AnnotationMarker)
		
		else -> illegalToken()
	}
}
