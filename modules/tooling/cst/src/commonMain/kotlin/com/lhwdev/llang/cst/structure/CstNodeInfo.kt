package com.lhwdev.llang.cst.structure

import com.lhwdev.utils.reflect.companionObject
import kotlin.reflect.KClass


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
	 * 1. If inside discardable node & CstParseContext.preventDiscard was **not** called:
	 *    - throw [DiscardException] to escape discardable node
	 *    - If no branch matches, go to 2.
	 * 2. (otherwise,) find branch with 'least error'. Conditions considered for this is:
	 *    - previous tree if exists; syntax can become broken for a short time while editing in IDE
	 *      (eg: editing `val` into `var`)
	 *    - TODO!
	 *
	 * Return null to disable graceful handling for this node. For example, keywords force the node
	 * to be parsed only in certain ways.
	 */
	fun dummyNode(): Node?
}

sealed interface CstSpecialNodeInfo<Node : CstNode> : CstNodeInfo<Node> {
	interface Inline<Node : CstNode> : CstSpecialNodeInfo<Node>
}

fun <Node : CstNode> CstNodeInfo<Node>.asInline(): CstNodeInfo<Node> =
	object : CstSpecialNodeInfo.Inline<Node>, CstNodeInfo<Node> by this {}


@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <Node : CstNode> KClass<Node>.nodeInfoOf(): CstNodeInfo<Node>? =
	companionObject()?.objectInstance as? CstNodeInfo<Node>

inline fun <reified Node : CstNode> nodeInfoOf(): CstNodeInfo<Node>? =
	Node::class.nodeInfoOf()
