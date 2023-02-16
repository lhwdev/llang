package com.lhwdev.llang.token


class SpanStateKey<T>(val defaultValue: T)


sealed class Span(var token: Token, val code: String) {
	class Plain(token: Token, code: String) : Span(token, code) {
		override fun toString(): String = "$token $code"
	}
	
	class PushState(
		token: Token,
		code: String,
		val stateKey: SpanStateKey<*>,
		val stateValue: Any?
	) : Span(token, code) {
		override fun toString(): String = "$token $code (+PushState $stateKey=$stateValue)"
	}
	
	class PopState(
		token: Token,
		code: String,
		val stateKey: SpanStateKey<*>
	) : Span(token, code) {
		override fun toString(): String = "$token $code (+PopState $stateKey)"
	}
	
}
