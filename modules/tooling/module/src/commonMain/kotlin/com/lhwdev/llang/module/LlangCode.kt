package com.lhwdev.llang.module


interface LlangCode : CharSequence {
	companion object {
		val Empty: LlangCode = object : LlangCode, CharSequence by "" {}
	}
}
