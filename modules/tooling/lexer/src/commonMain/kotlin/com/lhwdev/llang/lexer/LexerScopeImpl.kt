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
	
	override val currentIndex: LexerIndex
		get() = LexerIndex.Code(offset)
	
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
	
	override fun pushDiagnostic(diagnostic: Diagnostic, index: LexerIndex) { // TODO
		println("diagnostic pushed: ${with(StubDiagnosticContext) { diagnostic.getMessage() }}, index=$index")
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
	// TODO: optimizations around common IDE cases
	//       like user sequentially input characters in the same place
	//       state does not change; you can leave lookaheadIndex and cacheDeque of previous lexing
	private inner class LookaheadStateStack<T>(private val key: TokenStateKey<T>) : LexerStateStack<T> {
		private var lookaheadIndex = lexer.modification.oldCodeSpan.start // in 0..initialOffset
		
		private val cacheDeque = ArrayDeque<T>()
		
		override val current: T
			get() = if(cacheDeque.isNotEmpty()) {
				cacheDeque.last()
			} else {
				lookaheadForStateOrDefault()
			}
		
		/**
		 * Think of `a { b { c { d } e } f g }` and you are at `f`.
		 * You need to find current state, so you start traversing forward.
		 *
		 * When you meet `}`(PopState), you need to jump to matching `{`(PushState), and this may be cascaded.
		 * This may be simply implemented by recursion, but I thought using depth is simple enough.
		 * So, `}` increases depth so that you need one more `{` to pass by. And `{` decreases depth. If met `{` with
		 * `depth == 0`, done. You found it.
		 */
		private fun lookaheadForStateOrDefault(): T {
			var depth = 0
			while(lookaheadIndex > 0) { // note: case where lookaheadIndex == 0 (SHOULD) fails fast
				lookaheadIndex--
				when(val token = lexer.tokens[lookaheadIndex]) {
					is Token.Plain -> continue
					
					is Token.PopState -> if(token.stateKey == key) depth++
					
					is Token.PushState -> if(token.stateKey == key) {
						if(depth == 0) {
							@Suppress("UNCHECKED_CAST")
							val value = token.stateValue as T
							cacheDeque.addFirst(value)
							return value
						}
						depth--
					}
				}
			}
			return key.defaultValue
		}
		
		override fun push(value: T) {
			cacheDeque.addLast(value)
		}
		
		override fun pop(): T {
			lookaheadForStateOrDefault() // in case cacheDeque is empty, but you have more states to pop
			return cacheDeque.removeLast()
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
