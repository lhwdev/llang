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
	
	val stringQuote: Tokens.StringLiteral.Quote = Tokens.StringLiteral.Escaped, /* stub */
	
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
		return buildToken(Tokens.Eof)
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
			token(Tokens.Eol, length)
		}
		
		CharacterClass.whitespace -> token(Tokens.WhiteSpace) {
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
		"module" -> Tokens.Keyword.Module
		"group" -> Tokens.Keyword.Group
		"use" -> Tokens.Keyword.Use
		"class" -> Tokens.Keyword.Class
		"interface" -> Tokens.Keyword.Interface
		"object" -> Tokens.Keyword.Object
		"fun" -> Tokens.Keyword.Fun
		"impl" -> Tokens.Keyword.Impl
		"const" -> Tokens.Keyword.Const
		"val" -> Tokens.Keyword.Val
		"var" -> Tokens.Keyword.Var
		"true" -> Tokens.Keyword.True
		"false" -> Tokens.Keyword.False
		"if" -> Tokens.Keyword.If
		"else" -> Tokens.Keyword.Else
		"when" -> Tokens.Keyword.When
		"loop" -> Tokens.Keyword.Loop
		"while" -> Tokens.Keyword.While
		"for" -> Tokens.Keyword.For
		"do" -> Tokens.Keyword.Do
		"return" -> Tokens.Keyword.Return
		"break" -> Tokens.Keyword.Break
		"continue" -> Tokens.Keyword.Continue
		
		/// Tokens.Operation
		"is" -> Tokens.Operation.Is
		"in" -> Tokens.Operation.In
		
		// soft keywords are parsed from (tokens -> cst) parser
		
		else -> Tokens.Identifier
	}
}

context(LexerScope)
private fun handleNumber(): Token {
	// Note: In case of float with leading dot(`.123`), handleOther -> handleNumber
	
	// TODO: how to handle illegal identifier after number? shell I handle in cst parsing?
	//       like `12345abc`
	
	if(current == '0') when(ahead()) {
		'x' -> return token(Tokens.NumberLiteral.Hex) {
			advance(2)
			@Suppress("SpellCheckingInspection")
			advanceOneWhile { current in "0123456789abcdef" }
		}
		
		'b' -> return token(Tokens.NumberLiteral.Binary) {
			advance(2)
			advanceOneWhile { current in "01" }
		}
		
		else -> Unit
	}
	
	
	return token {
		var hasDot = false
		advanceOneWhile {
			when(current) {
				in '0'..'9' -> true
				'.' -> {
					hasDot = true
					true
				}
				
				else -> false
			}
		}
		
		if(hasDot) {
			Tokens.NumberLiteral.Float
		} else {
			Tokens.NumberLiteral.Integer
		}
	}
}

context(LexerScope)
private fun handleOther(): Token {
	val next = ahead()
	return when(current) {
		/// Tokens.Operation
		'+' -> when(next) {
			'=' -> token(Tokens.Operation.PlusEq, length = 2)
			else -> token(Tokens.Operation.Plus)
		}
		
		'-' -> when(next) {
			'=' -> token(Tokens.Operation.MinusEq, length = 2)
			'>' -> token(Tokens.Operation.ArrowRight, length = 2)
			else -> token(Tokens.Operation.Minus)
		}
		
		'*' -> token(Tokens.Operation.Times)
		
		'/' -> when(next) {
			'/' -> token(Tokens.Comment.Eol) { advanceBeforeEol() }
			'*' -> handleBlockComment() /// -> Tokens.Comment
			else -> token(Tokens.Operation.Divide)
		}
		
		'%' -> token(Tokens.Operation.Remainder)
		
		'=' -> when(next) {
			'=' -> when(ahead(2)) {
				'=' -> token(Tokens.Operation.IdentityEquals, length = 3)
				else -> token(Tokens.Operation.Equals, length = 2)
			}
			
			else -> token(Tokens.Operation.Eq, length = 1)
		}
		
		'!' -> when(next) {
			'=' -> when(ahead(2)) {
				'=' -> token(Tokens.Operation.NotIdentityEquals, length = 3)
				else -> token(Tokens.Operation.NotEquals, length = 2)
			}
			
			else -> when {
				matchesNext("is", offset = 1) -> token(Tokens.Operation.NotIs, length = 3)
				matchesNext("in", offset = 1) -> token(Tokens.Operation.NotIn, length = 3)
				else -> token(Tokens.Operation.Not)
			}
		}
		
		'<' -> when(next) {
			'=' -> token(Tokens.Operation.LtEq, length = 2)
			else -> token(Tokens.Operation.Lt)
		}
		
		'>' -> when(next) {
			'=' -> token(Tokens.Operation.GtEq, length = 2)
			else -> token(Tokens.Operation.Gt)
		}
		
		'&' -> when(next) {
			'&' -> token(Tokens.Operation.And, length = 2)
			else -> illegalToken()
		}
		
		'|' -> when(next) {
			'|' -> token(Tokens.Operation.And, length = 2)
			else -> illegalToken()
		}
		
		'.' -> @Suppress("IntroduceWhenSubject") when { // TODO: limit on adjacent tokens
			next == '.' -> when(ahead(2)) {
				'<' -> token(Tokens.Operation.RangeUntil, length = 3)
				else -> token(Tokens.Operation.RangeTo, length = 2)
			}
			// CharacterClass.isNumber(next) -> handleNumber() // we have tuple! (`tuple.0`)
			else -> token(Tokens.Operation.Dot)
		}
		
		'(' -> token(Tokens.Operation.LeftParen)
		')' -> token(Tokens.Operation.RightParen)
		'[' -> token(Tokens.Operation.LeftBracket)
		']' -> token(Tokens.Operation.RightBracket)
		'{' -> groupOpen(Tokens.Operation.LeftBrace)
		'}' -> groupClose(Tokens.Operation.RightBrace)
		
		'?' -> when(next) {
			'.' -> token(Tokens.Operation.SafeCall, length = 2)
			':' -> token(Tokens.Operation.Elvis, length = 2)
			else -> token(Tokens.Operation.PropagateError)
		}
		
		',' -> token(Tokens.Operation.Comma)
		
		':' -> token(Tokens.Operation.Colon)
		
		'#' -> when(next) {
			// #!/usr/bin/hello
			'!' -> token(Tokens.Comment.Shebang) { advanceBeforeEol() }
			
			// #[annotation]
			'[' -> token(Tokens.Operation.Annotation) // excluding [
			
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
		Tokens.Comment.LDocBlock
	} else {
		Tokens.Comment.Block
	}
}

context(LexerScope)
private fun handleString(): Token {
	val isRaw = matchesNext("\"\"", offset = 1)
	return if(isRaw) {
		token(Tokens.StringLiteral.Raw.Begin) {
			val context = lexerContext
			pushState(
				LexerContextKey,
				context.copy(
					stringQuote = Tokens.StringLiteral.Raw,
					stringDepth = context.stringDepth + 1
				)
			)
			advance(3)
		}
	} else {
		token(Tokens.StringLiteral.Escaped.Begin) {
			val context = lexerContext
			pushState(
				LexerContextKey,
				context.copy(
					stringQuote = Tokens.StringLiteral.Escaped,
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
		return buildToken(Tokens.StringLiteral.Literal)
	}
	
	return when(current) {
		'\\' -> when(ahead()) {
			// \\ \$ \n \r ...
			'\\', '$', 'n', 'r', 't', 'b', '\'', '"' -> token(Tokens.StringLiteral.EscapedLiteral, 2)
			// \u39A8
			'u' -> token(Tokens.StringLiteral.EscapedLiteral, 6)
			else -> token(Tokens.StringLiteral.EscapedLiteral) {
				advance(2)
				pushDiagnostic(LexerDiagnostic.IllegalStringEscape(currentSpan.toString()))
			}
		}
		
		// TODO: how to escape $ in raw string literal?
		'$' -> if(ahead() == '{') token(Tokens.StringLiteral.TemplateExpression) {
			val context = lexerContext
			pushState(
				LexerContextKey,
				context.copy(stringDepth = context.stringDepth + 1, groupDepth = context.groupDepth + 1)
			)
			advance(2)
		} else token(Tokens.StringLiteral.TemplateVariable) {
			advance() // $
			advanceOneWhile { CharacterClass.isMiddleWord(current) }
		}
		
		'"' -> when(lexerContext.stringQuote) {
			Tokens.StringLiteral.Escaped -> token(Tokens.StringLiteral.Escaped.End)
			Tokens.StringLiteral.Raw -> if(ahead() == '"') {
				if(ahead(2) == '"') {
					token(Tokens.StringLiteral.Raw.End, length = 3)
				} else {
					token(Tokens.StringLiteral.Literal)
				}
			} else {
				token(Tokens.StringLiteral.Literal) // should be joined when parsing
			}
			
			else -> error("unknown quote")
		}
		
		else -> error("logic error; check advanceOneWhile above")
	}
}
