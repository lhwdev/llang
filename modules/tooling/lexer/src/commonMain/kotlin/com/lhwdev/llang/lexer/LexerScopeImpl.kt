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
	
	protected abstract fun <T> stateStackOf(key: TokenStateKey<T>): LexerStateStack<T>
	
	override fun <T> pushState(key: TokenStateKey<T>, value: T) {
		if(currentTokenStateOperation != null)
			error("does not support multiple operations at once; previous = $currentTokenStateOperation, new = push($key, $value)")
		currentTokenStateOperation = StateOperation.Push(key, value)
		stateStackOf(key).push(value)
	}
	
	override fun <T> popState(key: TokenStateKey<T>): T {
		if(currentTokenStateOperation != null)
			error("does not support multiple operations at once; previous = $currentTokenStateOperation, new = pop($key)")
		currentTokenStateOperation = StateOperation.Pop(key)
		return stateStackOf(key).pop()
	}
	
	override fun <T> getCurrentState(key: TokenStateKey<T>): T =
		stateStackOf(key).current
	
	override fun pushDiagnostic(diagnostic: Diagnostic) { // TODO
		println("diagnostic pushed: ${with(StubDiagnosticContext) { diagnostic.getMessage() }}")
	}
}


internal class LexerScopeOnInitialization(lexer: Lexer) : LexerScopeImplBase(lexer, offset = 0) {
	private val stateStack = IdentityHashMap<TokenStateKey<*>, LexerStateStack<Any?>>()
	
	@Suppress("UNCHECKED_CAST")
	override fun <T> stateStackOf(key: TokenStateKey<T>) =
		stateStack.getOrPut(key) { LexerStateStack(key) as LexerStateStack<Any?> } as LexerStateStack<T>
}

internal class LexerScopeIncremental(lexer: Lexer) : LexerScopeImplBase(
	lexer = lexer,
	offset = lexer.modification.oldCodeSpan.start
) {
	private inner class LookaheadStateStack<T>(private val key: TokenStateKey<T>) : LexerStateStack<T> {
		private var lookaheadIndex = lexer.modification.oldCodeSpan.start // in 0..initialOffset
		
		private val cacheDeque = ArrayDeque<T>()
		
		override val current: T
			get() = when {
				cacheDeque.isNotEmpty() -> cacheDeque.last()
				lookaheadIndex == 0 -> key.defaultValue
				else -> lookaheadForStateOrDefault()
			}
		
		private fun lookaheadForStateOrDefault(): T {
			while(lookaheadIndex > 0) {
				lookaheadIndex--
				when(val token = lexer.tokens[lookaheadIndex]) {
					is Token.Plain -> TODO()
					is Token.PopState -> TODO()
					is Token.PushState -> TODO()
				}
				
			}
			return key.defaultValue
		}
		
		override fun push(value: T) {
			TODO("Not yet implemented")
		}
		
		override fun pop(): T {
			TODO("Not yet implemented")
		}
	}
	
	// TODO: maybe leaving depth with tokens good for optimization?
	private val stateStack = IdentityHashMap<TokenStateKey<*>, LookaheadStateStack<Any?>>()
	
	@Suppress("UNCHECKED_CAST")
	override fun <T> stateStackOf(key: TokenStateKey<T>) =
		stateStack.getOrPut(key) { LookaheadStateStack(key) as LookaheadStateStack<Any?> } as LexerStateStack<T>
}


internal interface LexerStateStack<T> {
	val current: T
	
	fun push(value: T)
	
	fun pop(): T
}

internal fun <T> LexerStateStack(key: TokenStateKey<T>): LexerStateStack<T> = object : LexerStateStack<T> {
	private val deque = ArrayDeque<T>()
	
	override val current: T
		get() = if(deque.isEmpty()) {
			key.defaultValue
		} else {
			deque.last()
		}
	
	override fun push(value: T) {
		deque.addLast(value)
	}
	
	override fun pop(): T {
		return deque.removeLast()
	}
}
