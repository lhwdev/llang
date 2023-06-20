package com.lhwdev.llang.parser.statement

import com.lhwdev.llang.parser.CstParseContext


fun CstParseContext.cstStatements() {
	// checking if end of statement
	// rules:
	// 1. If unclosed group remains, follow following lines.
	// 2. if following line can be parsed successfully, end current line here.
	//    (empty line is skipped)
	//    -> this will create too much recursion
	// 3. if following line starts with some binary operator, merge two.
	//
	// Problems: this is like a monkey-patch
	
}
