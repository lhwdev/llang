package com.lhwdev.llang.lexer

import com.lhwdev.llang.diagnostic.Diagnostic
import com.lhwdev.llang.diagnostic.StubDiagnosticContext
import com.lhwdev.llang.module.LlangCode
import com.lhwdev.llang.token.*
import com.lhwdev.utils.collection.IdentityHashMap


class Lexer(code: LlangCode) {
	var code: LlangCode = code
		private set
	
	var tokens: List<Token> = emptyList()
		private set
	
	fun parse() {
		// TODO: this does not support IC
		val scope = LexerScopeImpl(code)
		val run = LexerRun(scope)
		
		val list = ArrayList<Token>()
		while(true) {
			val token = run.advance()
			if(token.kind == Tokens.Eof) break
			list += token
		}
		this.tokens = list
	}
	
	fun updateCode(code: LlangCode) {
		TODO()
	}
	
	fun discardAndSetCode(code: LlangCode) {
		TODO()
	}
}


private class LexerScopeImpl(
	private val code: LlangCode,
	private var offset: Int = 0,
) : LexerScope {
	private sealed class StateOperation {
		class Push(val key: TokenStateKey<*>, val value: Any?) : StateOperation()
		class Pop(val key: TokenStateKey<*>) : StateOperation()
	}
	
	private var start = -1
	private val stateStack = IdentityHashMap<TokenStateKey<*>, ArrayDeque<Any?>>()
	private var currentTokenStateOperation: StateOperation? = null
	
	override val following: CharSequence = object : CharSequence {
		override val length: Int
			get() = code.length - offset
		
		override fun get(index: Int): Char =
			code[offset + index]
		
		override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
			code.subSequence(startIndex + offset, endIndex + offset)
		
		override fun toString(): String =
			code.substring(offset)
	}
	
	override fun advance(count: Int) {
		if(count < 0) error("cannot advance by $count")
		offset += count
	}
	
	override fun markStart() {
		start = offset
	}
	
	override fun moveToStart() {
		offset = start
	}
	
	override fun buildToken(token: LlTokenKind): Token = when(val operation = currentTokenStateOperation) {
		null -> Token.Plain(token, currentSpan.toString())
		
		is StateOperation.Push -> Token.PushState(
			kind = token,
			code = currentSpan.toString(),
			stateKey = operation.key,
			stateValue = operation.value
		)
		
		is StateOperation.Pop -> Token.PopState(
			kind = token,
			code = currentSpan.toString(),
			stateKey = operation.key
		)
	}.also { currentTokenStateOperation = null }
	
	override val currentSpan: CharSequence = object : CharSequence {
		override val length: Int
			get() = offset - start
		
		override fun get(index: Int): Char {
			if(index >= length) throw IndexOutOfBoundsException(index)
			return code[start + index]
		}
		
		override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
			if(endIndex >= length) throw IndexOutOfBoundsException(endIndex)
			return code.subSequence(start + startIndex, start + endIndex)
		}
		
		override fun toString(): String =
			code.substring(start, offset)
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
