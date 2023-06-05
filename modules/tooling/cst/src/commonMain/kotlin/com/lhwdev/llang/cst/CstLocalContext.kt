package com.lhwdev.llang.cst

import com.lhwdev.llang.tokenizer.ParseLocation


interface CstLocalContextSource {
	fun <T> getLocalContext(key: CstLocalContextKey<T>): T
}

interface CstLocalContextKey<T> {
	context(CstCodeSource)
	val defaultValue: T
}


class CstLocalContext(
	val parent: CstLocalContext? = null,
	val location: ParseLocation,
) {
	companion object : CstLocalContextKey<CstLocalContext> {
		context(CstCodeSource)
		override val defaultValue: CstLocalContext
			get() = CstLocalContext(location = ParseLocation.Declarations)
	}
	
}

val CstLocalContextSource.localContext: CstLocalContext
	get() = getLocalContext(CstLocalContext)
