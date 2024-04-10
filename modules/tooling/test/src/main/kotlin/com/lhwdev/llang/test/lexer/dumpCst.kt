package com.lhwdev.llang.test.lexer

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeImpl
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties


private fun indent(depth: Int) = "  ".repeat(depth)

private val cstNodeProp = CstNodeImpl::class.memberProperties.map { it.name }.toSet()

fun CstNode.dumpReflect(key: String, depth: Int = 0) {
	val cl = this::class
	println(indent(depth) + "${Color.cyan(key)}=${cl.qualifiedName}")
	if(depth > 10) {
		println("too much recurse!")
		return
	}
	val properties = cl.memberProperties.filter { it.name !in cstNodeProp }
	for(property in properties) {
		@Suppress("UNCHECKED_CAST")
		val value = (property as KProperty1<CstNode, *>).get(this)
		when(value) {
			is CstNode -> value.dumpReflect(property.name, depth = depth + 1)
			is List<*> -> if(value.any { it is CstNode }) {
				println(indent(depth + 1) + "${Color.cyan(property.name)}=List")
				for((index, item) in value.withIndex()) {
					if(item is CstNode) item.dumpReflect("$index", depth + 2)
				}
			}
			
			else -> println(indent(depth + 1) + "${property.name}=$value")
		}
	}
}
