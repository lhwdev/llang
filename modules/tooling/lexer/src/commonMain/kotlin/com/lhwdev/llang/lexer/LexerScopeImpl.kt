package com.lhwdev.llang.lexer

import com.lhwdev.llang.diagnostic.Diagnostic
import com.lhwdev.llang.diagnostic.StubDiagnosticContext
import com.lhwdev.llang.token.LlTokenKind
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenStateKey
import com.lhwdev.utils.collection.IdentityHashMap


internal abstract class LexerScopeImplBase(
	protected val lexer: Lexer,
	protected var offset: Int,
) : LexerScope {
	private sealed class StateOperation {
		abstract fun toToken(kind: LlTokenKind, code: String): Token
		
		class Push(val key: TokenStateKey<*>, val value: Any?) : StateOperation() {
			override fun toToken(kind: LlTokenKind, code: String): Token =
				Token.PushState(kind, code, key, value)
		}
		
		class Pop(val key: TokenStateKey<*>) : StateOperation() {
			override fun toToken(kind: LlTokenKind, code: String): Token =
				Token.PopState(kind, code, key)
		}
	}
	
	private var start = -1
	private val stateStack = IdentityHashMap<TokenStateKey<*>, ArrayDeque<Any?>>()
	private var currentTokenStateOperation: StateOperation? = null
	
	override val following: CharSequence = object : CharSequence {
		override val length: Int
			get() = lexer.code.length - offset
		
		override fun get(index: Int): Char =
			lexer.code[offset + index]
		
		override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
			lexer.code.subSequence(startIndex + offset, endIndex + offset)
		
		override fun toString(): String =
			lexer.code.substring(offset)
	}
	
	override fun advance(count: Int) {
		if(count < 0) error("cannot advance by negative. (by $count)")
		offset += count
	}
	
	override fun markStart() {
		start = offset
	}
	
	override fun buildToken(token: LlTokenKind): Token {
		val operation = currentTokenStateOperation
		return if(operation == null) {
			Token.Plain(token, currentSpan.toString())
		} else {
			currentTokenStateOperation = null
			operation.toToken(token, currentSpan.toString())
		}
	}
	
	override val currentSpan: CharSequence = object : CharSequence {
		override val length: Int
			get() = offset - start
		
		override fun get(index: Int): Char {
			if(index >= length) throw IndexOutOfBoundsException(index)
			return lexer.code[start + index]
		}
		
		override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
			if(endIndex >= length) throw IndexOutOfBoundsException(endIndex)
			return lexer.code.subSequence(start + startIndex, start + endIndex)
		}
		
		override fun toString(): String =
			lexer.code.substring(start, offset)
	}
	
	
	@Suppress("UNCHECKED_CAST")
	private fun <T> stateStackOf(key: TokenStateKey<T>) =
		stateStack.getOrPut(key) { ArrayDeque() } as ArrayDeque<T>
	
	override fun <T> pushState(key: TokenStateKey<T>, value: T) {
		if(currentTokenStateOperation != null)
			error("does not support multiple operations at once; previous = $currentTokenStateOperation, new = push($key, $value)")
		currentTokenStateOperation = StateOperation.Push(key, value)
		stateStackOf(key).addLast(value)
	}
	
	override fun <T> popState(key: TokenStateKey<T>): T {
		if(currentTokenStateOperation != null)
			error("does not support multiple operations at once; previous = $currentTokenStateOperation, new = pop($key)")
		currentTokenStateOperation = StateOperation.Pop(key)
		return stateStackOf(key).removeLast()
	}
	
	override fun <T> getCurrentState(key: TokenStateKey<T>): T {
		val stack = stateStackOf(key)
		return if(stack.isEmpty()) {
			key.defaultValue
		} else {
			stack.last()
		}
	}
	
	override fun pushDiagnostic(diagnostic: Diagnostic) { // TODO
		println("diagnostic pushed: ${with(StubDiagnosticContext) { diagnostic.getMessage() }}")
	}
}


internal class LexerScopeOnInitialization(lexer: Lexer) : LexerScopeImplBase(lexer, offset = 0)

internal class LexerScopeIncremental(lexer: Lexer) : LexerScopeImplBase(
	lexer = lexer,
	offset = lexer.modification.oldCodeSpan.start
)
