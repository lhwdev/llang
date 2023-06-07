package com.lhwdev.utils.string


fun Char.isLineBreak(): Boolean =
	this == '\r' || this == '\n'
