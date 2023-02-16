package com.lhwdev.llang.lexer

import com.lhwdev.llang.lexer.code.*
import com.lhwdev.llang.token.LlToken
import com.lhwdev.llang.token.Span
import com.lhwdev.llang.token.SpanStateKey
import com.lhwdev.llang.token.Tokens


class LexerRun(private val scope: LexerScope) {
	fun advance(): Span = with(scope) {
		nextSpan()
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

private val LexerContextKey = SpanStateKey(defaultValue = LexerContext())

context(LexerScope)
private val lexerContext: LexerContext
	get() = LexerContextKey.value

context(LexerScope)
private fun nextSpan(): Span {
	if(eof) {
		markStart()
		return buildSpan(Tokens.Eof)
	}
	
	val context = lexerContext
	
	return if(context.stringDepth % 2 == 0) {
		nextStateRoot()
	} else {
		nextStateString()
	}
}

context(LexerScope)
private fun nextStateRoot(): Span {
	// only guaranteed one or more chars exist in [following].
	val first = current
	
	val span = when(CharacterClass(first)) {
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
			span(Tokens.Eol, length)
		}
		
		CharacterClass.whitespace -> span(Tokens.WhiteSpace) {
			advance()
			advanceOneWhile { CharacterClass.isWhitespace(current) }
		}
		
		CharacterClass.other -> handleOther()
	}
	
	return span
}

context(LexerScope)
private fun handleWord() = span {
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
private fun handleNumber(): Span {
	// Note: In case of float with leading dot(`.123`), handleOther -> handleNumber
	
	// TODO: how to handle illegal identifier after number? shell I handle in cst parsing?
	//       like `12345abc`
	
	if(current == '0') when(ahead()) {
		'x' -> return span(Tokens.NumberLiteral.Hex) {
			advance(2)
			@Suppress("SpellCheckingInspection")
			advanceOneWhile { current in "0123456789abcdef" }
		}
		
		'b' -> return span(Tokens.NumberLiteral.Binary) {
			advance(2)
			advanceOneWhile { current in "01" }
		}
		
		else -> Unit
	}
	
	
	return span {
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
private fun handleOther(): Span {
	val next = ahead()
	return when(current) {
		/// Tokens.Operation
		'+' -> when(next) {
			'=' -> span(Tokens.Operation.PlusEq, length = 2)
			else -> span(Tokens.Operation.Plus)
		}
		
		'-' -> when(next) {
			'=' -> span(Tokens.Operation.MinusEq, length = 2)
			'>' -> span(Tokens.Operation.ArrowRight, length = 2)
			else -> span(Tokens.Operation.Minus)
		}
		
		'*' -> span(Tokens.Operation.Times)
		
		'/' -> when(next) {
			'/' -> span(Tokens.Comment.Eol) { advanceBeforeEol() }
			'*' -> handleBlockComment() /// -> Tokens.Comment
			else -> span(Tokens.Operation.Divide)
		}
		
		'%' -> span(Tokens.Operation.Remainder)
		
		'=' -> when(next) {
			'=' -> when(ahead(2)) {
				'=' -> span(Tokens.Operation.IdentityEquals, length = 3)
				else -> span(Tokens.Operation.Equals, length = 2)
			}
			
			else -> span(Tokens.Operation.Eq, length = 1)
		}
		
		'!' -> when(next) {
			'=' -> when(ahead(2)) {
				'=' -> span(Tokens.Operation.NotIdentityEquals, length = 3)
				else -> span(Tokens.Operation.NotEquals, length = 2)
			}
			
			else -> when {
				matchesNext("is", offset = 1) -> span(Tokens.Operation.NotIs, length = 3)
				matchesNext("in", offset = 1) -> span(Tokens.Operation.NotIn, length = 3)
				else -> span(Tokens.Operation.Not)
			}
		}
		
		'<' -> when(next) {
			'=' -> span(Tokens.Operation.LtEq, length = 2)
			else -> span(Tokens.Operation.Lt)
		}
		
		'>' -> when(next) {
			'=' -> span(Tokens.Operation.GtEq, length = 2)
			else -> span(Tokens.Operation.Gt)
		}
		
		'&' -> when(next) {
			'&' -> span(Tokens.Operation.And, length = 2)
			else -> illegalSpan()
		}
		
		'|' -> when(next) {
			'|' -> span(Tokens.Operation.And, length = 2)
			else -> illegalSpan()
		}
		
		'.' -> @Suppress("IntroduceWhenSubject") when { // TODO: limit on adjacent tokens
			next == '.' -> when(ahead(2)) {
				'<' -> span(Tokens.Operation.RangeUntil, length = 3)
				else -> span(Tokens.Operation.RangeTo, length = 2)
			}
			// CharacterClass.isNumber(next) -> handleNumber() // we have tuple! (`tuple.0`)
			else -> span(Tokens.Operation.Dot)
		}
		
		'(' -> span(Tokens.Operation.LeftParen)
		')' -> span(Tokens.Operation.RightParen)
		'[' -> span(Tokens.Operation.LeftBracket)
		']' -> span(Tokens.Operation.RightBracket)
		'{' -> groupOpen(Tokens.Operation.LeftBrace)
		'}' -> groupClose(Tokens.Operation.RightBrace)
		
		'?' -> when(next) {
			'.' -> span(Tokens.Operation.SafeCall, length = 2)
			':' -> span(Tokens.Operation.Elvis, length = 2)
			else -> illegalSpan()
		}
		
		':' -> span(Tokens.Operation.Colon)
		
		'#' -> when(next) {
			// #!/usr/bin/hello
			'!' -> span(Tokens.Comment.Shebang) { advanceBeforeEol() }
			
			// #[annotation]
			'[' -> span(Tokens.Operation.Annotation) // excluding [
			
			else -> illegalSpan()
		}
		
		/// Tokens.StringLiteral
		'"' -> handleString()
		
		
		else -> illegalSpan()
	}
}

context(LexerScope)
private fun groupOpen(token: LlToken): Span = span(token) {
	val context = lexerContext
	pushState(LexerContextKey, context.copy(groupDepth = context.groupDepth + 1))
	advance()
}

context(LexerScope)
private fun groupClose(token: LlToken): Span = span(token) {
	popState(LexerContextKey)
	advance()
}

context(LexerScope)
private fun handleBlockComment(): Span = span {
	// Block or LDocBlock; following starts with '/' '*'
	advance(2)
	
	val isLDoc = current == '*'
	if(isLDoc) advance() // TODO: tokenize more for LDoc highlighting
	
	// TODO: maybe split into more spans for IC optimization?
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
		}
	}
	
	if(isLDoc) {
		Tokens.Comment.LDocBlock
	} else {
		Tokens.Comment.Block
	}
}

context(LexerScope)
private fun handleString(): Span {
	val isRaw = matchesNext("\"\"", offset = 1)
	return if(isRaw) {
		span(Tokens.StringLiteral.Raw.Begin) {
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
		span(Tokens.StringLiteral.Escaped.Begin) {
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
private fun nextStateString(): Span {
	markStart()
	advanceOneWhile {
		val char = current
		char != '\\' && char != '$'
	}
	if(currentSpan.isNotEmpty()) {
		return buildSpan(Tokens.StringLiteral.Literal)
	}
	
	
}
