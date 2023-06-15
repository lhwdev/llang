package com.lhwdev.llang.tokenizer

import com.lhwdev.llang.parsing.util.parseError
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenKinds
import com.lhwdev.llang.tokenizer.source.CodeSource
import com.lhwdev.llang.tokenizer.source.advanceInWordNotEmpty
import com.lhwdev.llang.tokenizer.source.token


fun CodeSource.parseVariableKind(): Token = token {
	when(advanceInWordNotEmpty().toString()) {
		"val" -> TokenKinds.Keyword.Val
		"const" -> TokenKinds.Keyword.Const
		"var" -> TokenKinds.Keyword.Var
		else -> parseError("not variable")
	}
}

fun CodeSource.parseKeyword(): Token = token {
	// handle hard keywords
	when(advanceInWordNotEmpty().toString()) {
		/// TokenKinds.Operation: handled by cstExpression
		// "is" -> TokenKinds.Operation.Expression.Is
		// "in" -> TokenKinds.Operation.Expression.In
		
		
		/// TokenKinds.Keyword
		"module" -> TokenKinds.Keyword.Module
		"group" -> TokenKinds.Keyword.Group
		"use" -> TokenKinds.Keyword.Use
		
		"class" -> TokenKinds.Keyword.Class
		"interface" -> TokenKinds.Keyword.Interface
		"object" -> TokenKinds.Keyword.Object
		"fun" -> TokenKinds.Keyword.Fun
		"impl" -> TokenKinds.Keyword.Impl
		"type" -> TokenKinds.Keyword.Type
		// "val" -> TokenKinds.Keyword.Val // -> parseVariableKind
		// "const" -> TokenKinds.Keyword.Const
		// "var" -> TokenKinds.Keyword.Var
		
		"true" -> TokenKinds.Keyword.True
		"false" -> TokenKinds.Keyword.False
		
		"if" -> TokenKinds.Keyword.If
		"else" -> TokenKinds.Keyword.Else
		"when" -> TokenKinds.Keyword.When
		"loop" -> TokenKinds.Keyword.Loop
		"while" -> TokenKinds.Keyword.While
		"for" -> TokenKinds.Keyword.For
		
		"return" -> TokenKinds.Keyword.Return
		"break" -> TokenKinds.Keyword.Break
		"continue" -> TokenKinds.Keyword.Continue
		
		else -> parseError("Expected special")
	}
}

fun CodeSource.parseSoftKeyword(): Token = token {
	when(advanceInWordNotEmpty().toString()) {
		/// TokenKinds.SoftKeyword
		"constructor" -> TokenKinds.SoftKeyword.Constructor
		"init" -> TokenKinds.SoftKeyword.Constructor
		"get" -> TokenKinds.SoftKeyword.Get
		"set" -> TokenKinds.SoftKeyword.Set
		"field" -> TokenKinds.SoftKeyword.Field
		"where" -> TokenKinds.SoftKeyword.Where
		"by" -> TokenKinds.SoftKeyword.By
		// "in" -> TokenKinds.SoftKeyword.ForIn // handled by cstFor
		
		else -> parseError("Expected special")
	}
}

fun CodeSource.parseModifier(): Token = token {
	when(advanceInWordNotEmpty().toString()) {
		/// TokenKinds.Modifier
		"public" -> TokenKinds.Modifier.Public
		"internal" -> TokenKinds.Modifier.Internal
		"protected" -> TokenKinds.Modifier.Protected
		"private" -> TokenKinds.Modifier.Private
		
		"final" -> TokenKinds.Modifier.Final
		"open" -> TokenKinds.Modifier.Open
		"sealed" -> TokenKinds.Modifier.Sealed
		"abstract" -> TokenKinds.Modifier.Abstract
		
		"context" -> TokenKinds.Modifier.Context
		"expect" -> TokenKinds.Modifier.Expect
		"actual" -> TokenKinds.Modifier.Actual
		
		"enum" -> TokenKinds.Modifier.Enum
		"value" -> TokenKinds.Modifier.Value
		"inner" -> TokenKinds.Modifier.Inner
		"companion" -> TokenKinds.Modifier.Companion
		
		"infix" -> TokenKinds.Modifier.Infix
		"suspend" -> TokenKinds.Modifier.Suspend
		"inline" -> TokenKinds.Modifier.Inline
		
		"override" -> TokenKinds.Modifier.Override
		
		"in" -> TokenKinds.Modifier.VarianceIn
		"out" -> TokenKinds.Modifier.VarianceOut
		"referential" -> TokenKinds.Modifier.Referential
		"erased" -> TokenKinds.Modifier.Erased
		
		"vararg" -> TokenKinds.Modifier.Vararg
		"crossinline" -> TokenKinds.Modifier.Crossinline
		
		else -> parseError("Expected special")
	}
}
