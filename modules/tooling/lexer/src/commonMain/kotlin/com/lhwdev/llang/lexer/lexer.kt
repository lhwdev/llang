package com.lhwdev.llang.lexer

import com.lhwdev.llang.lexer.code.*
import com.lhwdev.llang.token.*


class LexerRun(private val scope: LexerScope) {
	fun advance(): Token = with(scope) {
		nextToken()
	}
}


private data class LexerContext(
	// val commentDepth: Int = 0,
	
	val stringQuote: TokenKinds.StringLiteral.Quote = TokenKinds.StringLiteral.Escaped, /* stub */
	
	/**
	 * If `stringDepth % 2 == 0`, we parse expression or anything.
	 * If `stringDepth % 2 == 1`, we parse string literal content.
	 *
	 * `"(depth=1) hello, world! ${ (depth=2) query(3, "(depth=3)lhwdev") }"`
	 */
	val stringDepth: Int = 0,
	
	/**
	 * Used to pair { and } in string literal. No other use here.
	 * Note that this is only modified by {}, not () or [], <>. (tracking < / > is impossible; is it lt, or group?)
	 */
	val groupDepth: Int = 0,
)

private val LexerContextKey = TokenStateKey(defaultValue = LexerContext(), debugName = "lexerState")

context(LexerScope)
private val lexerContext: LexerContext
	get() = LexerContextKey.value

context(LexerScope)
private fun nextToken(): Token {
	if(eof) {
		markStart()
		return buildToken(TokenKinds.Eof)
	}
	
	return if(lexerContext.stringDepth % 2 == 0) {
		nextStateRoot()
	} else {
		nextStateString()
	}
}

context(LexerScope)
private fun nextStateRoot(): Token {
	// only guaranteed one or more chars exist in [following].
	val first = current
	
	val token = when(CharacterClass(first)) {
		CharacterClass.word -> handleWord()
		
		CharacterClass.number -> handleNumber()
		
		CharacterClass.newline -> {
			val length = when(first) {
				'\r' -> when(ahead()) {
					'\n' -> 2
					else -> 1
				}
				
				'\n' -> 1
				
				else -> error("unreachable")
			}
			token(TokenKinds.Eol, length)
		}
		
		CharacterClass.whitespace -> token(TokenKinds.WhiteSpace) {
			advance()
			advanceOneWhile { CharacterClass.isWhitespace(current) }
		}
		
		CharacterClass.other -> handleOther()
	}
	
	return token
}

context(LexerScope)
private fun handleWord() = token {
	advance()
	advanceOneWhile { CharacterClass.isMiddleWord(current) }
	
	// handle hard keywords
	when(currentSpan.toString()) {
		/// Tokens.Keyword
		"module" -> TokenKinds.Keyword.Module
		"group" -> TokenKinds.Keyword.Group
		"use" -> TokenKinds.Keyword.Use
		"class" -> TokenKinds.Keyword.Class
		"interface" -> TokenKinds.Keyword.Interface
		"object" -> TokenKinds.Keyword.Object
		"fun" -> TokenKinds.Keyword.Fun
		"impl" -> TokenKinds.Keyword.Impl
		"const" -> TokenKinds.Keyword.Const
		"val" -> TokenKinds.Keyword.Val
		"var" -> TokenKinds.Keyword.Var
		"true" -> TokenKinds.Keyword.True
		"false" -> TokenKinds.Keyword.False
		"if" -> TokenKinds.Keyword.If
		"else" -> TokenKinds.Keyword.Else
		"when" -> TokenKinds.Keyword.When
		"loop" -> TokenKinds.Keyword.Loop
		"while" -> TokenKinds.Keyword.While
		"for" -> TokenKinds.Keyword.For
		"do" -> TokenKinds.Keyword.Do
		"return" -> TokenKinds.Keyword.Return
		"break" -> TokenKinds.Keyword.Break
		"continue" -> TokenKinds.Keyword.Continue
		
		/// Tokens.Operation
		"is" -> TokenKinds.Operation.Is
		"in" -> TokenKinds.Operation.In
		
		// soft keywords are parsed from (tokens -> cst) parser
		
		else -> TokenKinds.Identifier
	}
}

context(LexerScope)
private fun handleNumber(): Token {
	// Note: In case of float with leading dot(`.123`), handleOther -> handleNumber
	
	// TODO: how to handle illegal identifier after number? shell I handle in cst parsing?
	//       like `12345abc`
	
	if(current == '0') when(ahead()) {
		'x' -> return token(TokenKinds.NumberLiteral.Hex) {
			advance(2)
			@Suppress("SpellCheckingInspection")
			advanceOneWhile { current in "0123456789abcdef" }
		}
		
		'b' -> return token(TokenKinds.NumberLiteral.Binary) {
			advance(2)
			advanceOneWhile { current in "01" }
		}
		
		else -> Unit
	}
	
	return token {
		var hasDot = false
		var hasE = false
		
		advanceOneWhile {
			when(current) {
				in '0'..'9' -> true
				'.' -> {
					hasDot = true
					true
				}
				
				'e' -> {
					if(hasE) pushDiagnostic(LexerDiagnostic.IllegalNumber(message = "illegal scientific(e) notation"))
					hasE = true
					true // validation of notation is up to next parser(so far)
				}
				
				else -> false
			}
		}
		
		if(hasDot) {
			TokenKinds.NumberLiteral.Float
		} else {
			TokenKinds.NumberLiteral.Integer
		}
	}
}

context(LexerScope)
private fun handleOther(): Token {
	val next = ahead()
	return when(current) {
		/// Tokens.Operation
		'+' -> when(next) {
			'=' -> token(TokenKinds.Operation.PlusEq, length = 2)
			else -> token(TokenKinds.Operation.Plus)
		}
		
		'-' -> when(next) {
			'=' -> token(TokenKinds.Operation.MinusEq, length = 2)
			'>' -> token(TokenKinds.Operation.ArrowRight, length = 2)
			else -> token(TokenKinds.Operation.Minus)
		}
		
		'*' -> token(TokenKinds.Operation.Times)
		
		'/' -> when(next) {
			'/' -> token(TokenKinds.Comment.Eol) { advanceBeforeEol() }
			'*' -> handleBlockComment() /// -> Tokens.Comment
			else -> token(TokenKinds.Operation.Divide)
		}
		
		'%' -> token(TokenKinds.Operation.Remainder)
		
		'=' -> when(next) {
			'=' -> when(ahead(2)) {
				'=' -> token(TokenKinds.Operation.IdentityEquals, length = 3)
				else -> token(TokenKinds.Operation.Equals, length = 2)
			}
			
			else -> token(TokenKinds.Operation.Eq, length = 1)
		}
		
		'!' -> when(next) {
			'=' -> when(ahead(2)) {
				'=' -> token(TokenKinds.Operation.NotIdentityEquals, length = 3)
				else -> token(TokenKinds.Operation.NotEquals, length = 2)
			}
			
			else -> when {
				matchesNext("is", offset = 1) -> token(TokenKinds.Operation.NotIs, length = 3)
				matchesNext("in", offset = 1) -> token(TokenKinds.Operation.NotIn, length = 3)
				else -> token(TokenKinds.Operation.Not)
			}
		}
		
		'<' -> when(next) {
			'=' -> token(TokenKinds.Operation.LtEq, length = 2)
			else -> token(TokenKinds.Operation.Lt)
		}
		
		'>' -> when(next) {
			'=' -> token(TokenKinds.Operation.GtEq, length = 2)
			else -> token(TokenKinds.Operation.Gt)
		}
		
		'&' -> when(next) {
			'&' -> token(TokenKinds.Operation.And, length = 2)
			else -> illegalToken()
		}
		
		'|' -> when(next) {
			'|' -> token(TokenKinds.Operation.And, length = 2)
			else -> illegalToken()
		}
		
		'.' -> @Suppress("IntroduceWhenSubject") when { // TODO: limit on adjacent tokens
			next == '.' -> when(ahead(2)) {
				'<' -> token(TokenKinds.Operation.RangeUntil, length = 3)
				else -> token(TokenKinds.Operation.RangeTo, length = 2)
			}
			// CharacterClass.isNumber(next) -> handleNumber() // we have tuple! (`tuple.0`)
			else -> token(TokenKinds.Operation.Dot)
		}
		
		'(' -> token(TokenKinds.Operation.LeftParen)
		')' -> token(TokenKinds.Operation.RightParen)
		'[' -> token(TokenKinds.Operation.LeftBracket)
		']' -> token(TokenKinds.Operation.RightBracket)
		'{' -> groupOpen(TokenKinds.Operation.LeftBrace)
		'}' -> groupClose(TokenKinds.Operation.RightBrace)
		
		'?' -> when(next) {
			'.' -> token(TokenKinds.Operation.SafeCall, length = 2)
			':' -> token(TokenKinds.Operation.Elvis, length = 2)
			else -> token(TokenKinds.Operation.PropagateError)
		}
		
		',' -> token(TokenKinds.Operation.Comma)
		
		':' -> token(TokenKinds.Operation.Colon)
		
		'#' -> when(next) {
			// #!/usr/bin/hello
			'!' -> token(TokenKinds.Comment.Shebang) { advanceBeforeEol() }
			
			// #[annotation]
			'[' -> token(TokenKinds.Operation.Annotation) // excluding [
			
			else -> illegalToken()
		}
		
		/// Tokens.StringLiteral
		'"' -> handleString()
		
		
		else -> illegalToken()
	}
}

context(LexerScope)
private fun groupOpen(token: LlTokenKind): Token = token(token) {
	val context = lexerContext
	pushState(LexerContextKey, context.copy(groupDepth = context.groupDepth + 1))
	advance()
}

context(LexerScope)
private fun groupClose(token: LlTokenKind): Token = token(token) {
	popState(LexerContextKey)
	advance()
}

context(LexerScope)
private fun handleBlockComment(): Token = token {
	// Block or LDocBlock; following starts with '/' '*'
	advance(2)
	
	val isLDoc = current == '*'
	if(isLDoc) advance() // TODO: tokenize more for LDoc highlighting
	
	// TODO: maybe split into more tokens for IC optimization?
	var depth = 1
	
	while(true) {
		when {
			matchesNext("/*") -> {
				depth += 1
				advance(2)
			}
			
			matchesNext("*/") -> {
				depth -= 1
				advance(2)
				if(depth == 0) break
			}
			
			else -> advance()
		}
	}
	
	if(isLDoc) {
		TokenKinds.Comment.LDocBlock
	} else {
		TokenKinds.Comment.Block
	}
}

context(LexerScope)
private fun handleString(): Token {
	val isRaw = matchesNext("\"\"", offset = 1)
	return if(isRaw) {
		token(TokenKinds.StringLiteral.Raw.Begin) {
			val context = lexerContext
			pushState(
				LexerContextKey,
				context.copy(
					stringQuote = TokenKinds.StringLiteral.Raw,
					stringDepth = context.stringDepth + 1
				)
			)
			advance(3)
		}
	} else {
		token(TokenKinds.StringLiteral.Escaped.Begin) {
			val context = lexerContext
			pushState(
				LexerContextKey,
				context.copy(
					stringQuote = TokenKinds.StringLiteral.Escaped,
					stringDepth = context.stringDepth + 1
				)
			)
			advance(1)
		}
	}
}

context(LexerScope)
private fun nextStateString(): Token {
	markStart()
	advanceOneWhile {
		val char = current
		char != '\\' && char != '$' && char != '"'
	}
	if(currentSpan.isNotEmpty()) {
		return buildToken(TokenKinds.StringLiteral.Literal)
	}
	
	return when(current) {
		'\\' -> when(ahead()) {
			// \\ \$ \n \r ...
			'\\', '$', 'n', 'r', 't', 'b', '\'', '"' -> token(TokenKinds.StringLiteral.EscapedLiteral, 2)
			// \u39A8
			'u' -> token(TokenKinds.StringLiteral.EscapedLiteral, 6)
			else -> token(TokenKinds.StringLiteral.EscapedLiteral) {
				advance(2)
				pushDiagnostic(LexerDiagnostic.IllegalStringEscape(currentSpan.toString()))
			}
		}
		
		// TODO: how to escape $ in raw string literal?
		'$' -> if(ahead() == '{') token(TokenKinds.StringLiteral.TemplateExpression) {
			val context = lexerContext
			pushState(
				LexerContextKey,
				context.copy(stringDepth = context.stringDepth + 1, groupDepth = context.groupDepth + 1)
			)
			advance(2)
		} else token(TokenKinds.StringLiteral.TemplateVariable) {
			advance() // $
			advanceOneWhile { CharacterClass.isMiddleWord(current) }
		}
		
		'"' -> when(lexerContext.stringQuote) {
			TokenKinds.StringLiteral.Escaped -> token(TokenKinds.StringLiteral.Escaped.End)
			TokenKinds.StringLiteral.Raw -> if(ahead() == '"') {
				if(ahead(2) == '"') {
					token(TokenKinds.StringLiteral.Raw.End, length = 3)
				} else {
					token(TokenKinds.StringLiteral.Literal)
				}
			} else {
				token(TokenKinds.StringLiteral.Literal) // should be joined when parsing
			}
			
			else -> error("unknown quote")
		}
		
		else -> error("logic error; check advanceOneWhile above")
	}
}
