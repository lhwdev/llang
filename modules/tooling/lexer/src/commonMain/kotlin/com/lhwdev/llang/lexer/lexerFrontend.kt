package com.lhwdev.llang.lexer

import com.lhwdev.llang.module.LlangCode
import com.lhwdev.llang.token.*
import com.lhwdev.utils.collection.IdentityHashMap


class Lexer(code: LlangCode) {
	var code: LlangCode = code
		private set
	
	var spans: List<Span> = emptyList()
		private set
	
	fun parse() {
		// TODO: this does not support IC
		val scope = LexerScopeImpl(code)
		val run = LexerRun(scope)
		
		val list = ArrayList<Span>()
		while(true) {
			val span = run.advance()
			if(span.token == Tokens.Eof) break
			list += span
		}
		this.spans = list
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
		class Push(val key: SpanStateKey<*>, val value: Any?) : StateOperation()
		class Pop(val key: SpanStateKey<*>) : StateOperation()
	}
	
	private var start = -1
	private val stateStack = IdentityHashMap<SpanStateKey<*>, ArrayDeque<Any?>>()
	private var currentSpanStateOperation: StateOperation? = null
	
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
	
	override fun buildSpan(token: LlToken): Span = when(val operation = currentSpanStateOperation) {
		null -> Span.Plain(token, currentSpan.toString())
		
		is StateOperation.Push -> Span.PushState(
			token = token,
			code = currentSpan.toString(),
			stateKey = operation.key,
			stateValue = operation.value
		)
		
		is StateOperation.Pop -> Span.PopState(
			token = token,
			code = currentSpan.toString(),
			stateKey = operation.key
		)
	}.also { currentSpanStateOperation = null }
	
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
	private fun <T> stateStackOf(key: SpanStateKey<T>) =
		stateStack.getOrPut(key) { ArrayDeque() } as ArrayDeque<T>
	
	override fun <T> pushState(key: SpanStateKey<T>, value: T) {
		if(currentSpanStateOperation != null)
			error("does not support multiple operations at once; previous = $currentSpanStateOperation, new = push($key, $value)")
		currentSpanStateOperation = StateOperation.Push(key, value)
		stateStackOf(key).addLast(value)
	}
	
	override fun <T> popState(key: SpanStateKey<T>): T {
		if(currentSpanStateOperation != null)
			error("does not support multiple operations at once; previous = $currentSpanStateOperation, new = pop($key)")
		currentSpanStateOperation = StateOperation.Pop(key)
		return stateStackOf(key).removeLast()
	}
	
	override fun <T> getCurrentState(key: SpanStateKey<T>): T {
		val stack = stateStackOf(key)
		return if(stack.isEmpty()) {
			key.defaultValue
		} else {
			stack.last()
		}
	}
}
