package com.lhwdev.llang.test.lexer

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.parser.expression.cstExpression
import com.lhwdev.llang.parser.node
import kotlin.time.measureTime



fun main() {
	fun <T : CstNode> parse(code: String, init: TextParseContext.() -> T): T {
		println("Input code: $code")
		var node: T? = null
		val context = TextParseContext(code)
		val duration = measureTime {
			context.withRootGroup {
				try {
					node = init()
				} catch(th: Throwable) {
					println("ERROR!")
					throw th
				}
			}
		}
		println("== parse took $duration")
		println("result code = ${(node!!.tree as Group).contentToString()}")
		node!!.dumpReflect(key = "")
		node!!.dumpTree(context)
		
		println()
		return node!!
	}
	
	// parse("1 2") {
	// 	node {
	// 		markChildrenAsDetached(peek = true)
	// 		val a = node {
	// 			CstConstLiteral.Number(
	// 				cstLeafNode(
	// 					TokenKinds.Comment,
	// 					"1",
	// 				).token,
	// 			)
	// 		}
	// 		val b = node {
	// 			CstConstLiteral.Number(
	// 				cstLeafNode(
	// 					TokenKinds.Comment,
	// 					"2",
	// 				).token,
	// 			)
	// 		}
	//
	// 		endChildrenAsDetached()
	//
	// 		node {
	// 			acceptChildNode(a)
	// 			acceptChildNode(b)
	// 			CstTuple(listOf(a, b))
	// 		}
	// 	}
	// }
	
	parse("1+2") {
		cstExpression()
	}
	
	val expr = parse("1 +  2") {
		// rawNode(CstParseContext.NodeKind.Peek) {
		cstExpression()
		// }
	}
	
	parse("1 +  2") {
		println("accept!")
		node {
			acceptChildNode(expr)
		}
	}
	
	return
	
	parse(
		"""println("hello, ${'$'}name! ${'$'}{1 + 2}", a = 123 + 0x14)""".trimIndent(),
	) {
		cstExpression()
	}
	// parse("1 * 2 + 3 - 4 / (5 + 6)") {
	// 	cstExpression()
	// }
	
	// parse("fun, class, object") {
	// 	disableAdjacentImplicitNode()
	// 	cstCommaSeparatedList(CstKeyword) { cstKeyword() }
	// }
	//
}


