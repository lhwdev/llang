package com.lhwdev.llang.cst

import com.lhwdev.llang.source.CodeSource


interface CstCodeSource : CodeSource {
	val currentSpan: CharSequence
	
	
}