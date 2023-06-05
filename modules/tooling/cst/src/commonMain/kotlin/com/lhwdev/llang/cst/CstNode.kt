package com.lhwdev.llang.cst

import com.lhwdev.utils.reflect.companionObject
import kotlin.reflect.KClass


interface CstNode

/**
 * This should be implemented as companion object of the class.
 */
interface CstNodeInfo<Node : CstNode> {
	/**
	 * Enables graceful handling.
	 * By default, all parsing errors implies 'discard current branch', but in some cases it would
	 * lead to overall parsing failure. All parsing function requires valid value to create
	 * resulting token/node. So graceful handling is impossible in this sense.
	 * This function is to put some 'dummy node' into node tree for parsing to be gracefully
	 * handled.
	 *
	 * For example, if wrong number literal exists, parser will do following steps:
	 * -
	 */
	fun dummyNode(): Node? = null
}

typealias CstNodeFactory<Node> = CstParseContext.() -> Node


@Suppress("UNCHECKED_CAST")
fun <Node : CstNode> KClass<Node>.nodeInfoOf(): CstNodeInfo<Node>? =
	companionObject()?.objectInstance as? CstNodeInfo<Node>


inline fun <reified Node : CstNode> nodeInfoOf(): CstNodeInfo<Node>? =
	Node::class.nodeInfoOf()
