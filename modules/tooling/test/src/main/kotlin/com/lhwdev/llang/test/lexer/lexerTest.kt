package com.lhwdev.llang.test.lexer

import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstKeyword
import com.lhwdev.llang.cst.tree.CstTreeNode
import com.lhwdev.llang.diagnostic.Diagnostic
import com.lhwdev.llang.parser.CstCodeSource
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.core.cstImplicitNodeOrNull
import com.lhwdev.llang.parser.core.cstKeyword
import com.lhwdev.llang.parser.expression.cstExpression
import com.lhwdev.llang.parser.util.cstCommaSeparatedList
import com.lhwdev.llang.parsing.ParseLocation
import com.lhwdev.llang.parsing.debug
import com.lhwdev.llang.parsing.parseError
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl
import com.lhwdev.llang.token.TokenKind
import com.lhwdev.llang.tokenizer.TokenizerContext
import com.lhwdev.llang.tokenizer.source.CodeSequence
import kotlin.time.measureTime


class TextCodeSource(
	private val parent: TextParseContext,
	val text: String,
	var offset: Int = 0,
	var spanStart: Int = 0,
	private val debugName: String = "Lexer",
) : CstCodeSource {
	class Snapshot(val offset: Int, val spanStart: Int) {
		fun discard() {
			// does nothing
		}
	}
	
	fun createSnapshot(): Snapshot = Snapshot(offset, spanStart)
	fun restoreToSnapshot(to: Snapshot) {
		offset = to.offset
		spanStart = to.spanStart
	}
	
	override val debugEnabled: Boolean
		get() = parent.debugEnabled
	
	override fun debug(line: String) {
		parent.debug(line)
	}
	
	override fun pushDiagnostic(diagnostic: Diagnostic) {
		println(diagnostic)
	}
	
	// override fun acceptToken(token: Token): Token {
	// 	// this parser is stub; need not recycle tokens
	// 	advance(token.code.length)
	// 	check(token.code.contentEquals(currentSpan)) {
	// 		"expected ${token.code}; got $currentSpan."
	// 	}
	// 	debug {
	// 		"${Color.blue("$debugName acceptToken")} ${token.kind} ${
	// 			Color.green(escape(token.code))
	// 		}"
	// 	}
	// 	spanStart = offset
	// 	return token
	// }
	//
	// override fun cloneForRead(): CstCodeSource {
	// 	val clone = TextCodeSource(parent, text, offset, spanStart, "${debugName}'")
	// 	debug {
	// 		"${Color.blue("$debugName cloneForRead")} remaining: ${
	// 			escape(clone.next.toString())
	// 		}"
	// 	}
	// 	return clone
	// }
	
	override fun close() {}
	
	override val next: CodeSequence = object : CodeSequence {
		override val length: Int
			get() = text.length - offset
		
		override fun get(index: Int): Char {
			val newIndex = offset + index
			if(newIndex >= text.length) return '\u0000'
			return text[newIndex]
		}
		
		override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
			text.subSequence(offset + startIndex, offset + endIndex)
		
		override fun toString(): String = text.substring(offset)
	}
	override val currentSpan: CharSequence = object : CharSequence {
		override val length: Int
			get() = offset - spanStart
		
		override fun get(index: Int): Char {
			if(index < 0 || index > length) throw IndexOutOfBoundsException()
			return text[spanStart + index]
		}
		
		override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
			if(startIndex < 0 || endIndex > length) throw IndexOutOfBoundsException()
			return text.subSequence(startIndex, endIndex)
		}
		
		override fun toString(): String = text.substring(spanStart, offset)
	}
	
	override fun advance(count: Int) {
		offset += count
	}
	
	private fun tokenTrace() = Throwable().stackTrace
		.drop(2)
		.takeWhile { it.className.startsWith("com.lhwdev.llang.tokenizer") }
		.filter { "$" !in it.methodName }
		.map { it.methodName }
		.mapIndexed { index, name -> if(index == 0) Color.yellow(name) else Color.lightGray(name) }
	
	override fun buildToken(kind: TokenKind): Token {
		val code = currentSpan.toString()
		val trace = if(debugTraceStack) tokenTrace() else listOf(Color.yellow("?"))
		
		debug(
			"${Color.blue("$debugName token")} " + "" +
				"${trace.joinToString(separator = Color.gray("<-"))} " +
				"$kind ${Color.green(escape(code))}" +
				" remaining=${next.take(10)}",
		)
		spanStart = offset
		return TokenImpl(kind, code)
	}
	
	override fun resetToSpanStart() {
		offset = spanStart
	}
	
	override var context: TokenizerContext = TokenizerContext(location = ParseLocation.Declarations)
}

class Following(
	var flags: CurrentGroup.Flags = CurrentGroup.Flags(),
)

data class CurrentGroup(
	var kind: CstParseContext.NodeKind,
	var nodeInfo: CstNodeInfo<*>?,
	val codeSnapshot: TextCodeSource.Snapshot?,
	val children: MutableList<Group> = mutableListOf(),
	val flags: Flags = Flags(),
	var beginIndex: Int,
	val debugInfo: CurrentDebugInfo,
	// var youCanInsertImplicitNode: Boolean = false,
) {
	data class Flags(
		var reAttached: Boolean = false,
		var detached: Detach? = null,
		var childrenAreDetached: Boolean = false,
		var disableAdjacentImplicitNode: Boolean = false,
		var vitalHint: Boolean = false,
		var preventDiscardHint: Boolean = false,
		var selfDiscardPrevented: Boolean = false,
		var allowCodeAccess: Boolean = false,
	)
	
	data class Detach(val from: CurrentGroup)
}

class CurrentDebugInfo(
	val name: String,
	val parsingStartedAtNs: Long,
	val implicitLevel: Int,
	val entries: MutableList<DebugEntry> = mutableListOf(),
)

sealed class DebugEntry {
	data class Line(val line: String) : DebugEntry()
	data class Child(val group: Group) : DebugEntry()
}

private object Debug {
	fun indent(level: Int) = "  ".repeat(level - 1)
}

data class Group(
	val kind: CstParseContext.NodeKind,
	val children: MutableList<Group>,
	val node: CstNode,
	val nodeInfo: CstNodeInfo<*>?,
	val flags: CurrentGroup.Flags,
	val span: IntRange,
	val debugInfo: DebugInfo,
	val error: GroupError?,
) : CstTreeNode {
	class GroupError(
		val throwable: Throwable?,
	)
	
	fun dump(depth: Int, context: TextParseContext) {
		// fun formatError(throwable: Throwable) =
		// 	"${throwable::class.java.simpleName}${if(throwable.message != null) ": ${throwable.message}" else ""}"
		
		if(debugInfo.implicitLevel == 1 && error != null && error.throwable == null) {
			// Implicit Node(discarded); so much hassle by them
			return
		}
		
		var kind = kind.toString()
		if(debugInfo.implicitLevel != 0) {
			kind = if(debugInfo.implicitLevel == 1) {
				"Implicit $kind"
			} else {
				"Implicit' $kind"
			}
		}
		if(flags.reAttached) {
			kind = "Attached $kind"
		} else if(flags.detached != null) {
			kind = "Detached $kind"
		}
		
		
		var name = debugInfo.name
		var functionName: String? = null
		var infoName: String? = null
		if(nodeInfo != null) {
			infoName = nodeInfo::class.java.name.substringAfterLast('.').substringBeforeLast('$')
			val inferredName = "${infoName.first().lowercase()}${infoName.drop(1)}"
			when {
				name == inferredName || name.startsWith(inferredName) -> {}
				
				name.first() == '(' -> {
					name = inferredName
				}
				
				else -> {
					if('$' in infoName) {
						// as is
						infoName = infoName.replace('$', '.')
					} else {
						functionName = name
						name = inferredName
					}
				}
			}
		}
		val fieldName = debugInfo.fieldName
		if(fieldName != null) {
			name = "$fieldName=$name"
		}
		
		if(error != null) {
			println(
				Debug.indent(depth) +
					(if(error.throwable != null) Color.red else Color.lightGray)(kind) +
					"(${if(error.throwable != null) "error" else "discarded"}) " +
					"${Color.bold(name)} " +
					// (if(error.throwable != null) "error=${Color.lightGray("'${formatError(error.throwable)}'")} " else "") +
					(if(functionName != null) "function=${Color.green(functionName)} " else "") +
					(if(infoName != null) "info=${Color.green(infoName)} " else "") +
					"span=${Color.lightGreen(escape(context.c.text.slice(span)))} " +
					"took=${Color.green("${debugInfo.parsingDurationNs / 10000 / 100f}ms")}",
			)
			if(error.throwable == null) return
		} else {
			println(
				"${Debug.indent(depth)}${
					(if(debugInfo.implicitLevel != 0) Color.dimCyan else Color.cyan)(
						kind,
					)
				}" +
					" ${Color.bold(name)} " +
					(if(functionName != null) "function=${Color.green(functionName)} " else "") +
					(if(infoName != null) "info=${Color.green(infoName)} " else "") +
					"span=${Color.lightGreen(escape(context.c.text.slice(span)))} " +
					"took=${Color.green("${debugInfo.parsingDurationNs / 10000 / 100f}ms")}",
			)
		}
		if(debugInfo.implicitLevel == 1) return
		
		val indent = Debug.indent(depth + 1)
		for(entry in debugInfo.entries) {
			when(entry) {
				is DebugEntry.Line -> println("${indent}${entry.line}")
				is DebugEntry.Child -> entry.group.dump(depth + 1, context)
			}
		}
	}
}

data class DebugInfo(
	val name: String,
	val nestedLevel: Int,
	val entries: List<DebugEntry>,
	val parsingDurationNs: Long,
	val implicitLevel: Int,
	var attachedTo: CurrentGroup,
	var fieldName: String? = null,
)

class TextParseContext(text: String) : CstParseContext {
	override val code: CstCodeSource
		get() {
			val current = current
			if(current.kind == CstParseContext.NodeKind.LeafNode || current.flags.allowCodeAccess) {
				return c
			} else {
				throw IllegalAccessException("do not access code in normal node")
			}
		}
	
	internal val c = TextCodeSource(this, text)
	
	private val groups = ArrayDeque<CurrentGroup>().also {
		it += CurrentGroup(
			kind = CstParseContext.NodeKind.Node,
			nodeInfo = null,
			codeSnapshot = null,
			beginIndex = 0,
			debugInfo = CurrentDebugInfo(
				name = "<root-node>",
				parsingStartedAtNs = 0,
				implicitLevel = 0,
			),
		)
	}
	private val discardableGroups = ArrayDeque<CurrentGroup>()
	
	private var following: Following? = null
	private val current get() = groups.last()
	private val parentOfCurrent get() = groups[groups.size - 2]
	private val closestDiscardable get() = discardableGroups.lastOrNull()
	
	override val debugEnabled: Boolean = true
	override fun debug(line: String) {
		if(debugEnabled) current.debugInfo.entries += DebugEntry.Line(line)
	}
	
	override fun disableAdjacentImplicitNode() {
		current.flags.disableAdjacentImplicitNode = true
	}
	
	private var insideConsumeImplicitNode = false
	private fun consumeImplicitNodeIfApplicable() {
		val current = current
		// if(!current.youCanInsertImplicitNode) {
		// 	return
		// }
		// current.youCanInsertImplicitNode = false
		if(!current.kind.node || current.kind == CstParseContext.NodeKind.LeafNode) {
			return
		}
		if(!current.flags.disableAdjacentImplicitNode && !insideConsumeImplicitNode) {
			insideConsumeImplicitNode = true
			try {
				cstImplicitNodeOrNull()
			} finally {
				insideConsumeImplicitNode = false
			}
		}
	}
	
	@CstParseContext.InternalApi
	override val alwaysRequireNodeInfo: Boolean
		get() = true
	
	@Suppress("UNUSED_PARAMETER")
	private fun beforeEnterGroup(
		parent: CurrentGroup,
		kind: CstParseContext.NodeKind,
		info: CstNodeInfo<*>?,
	) {
		if(parent.kind == CstParseContext.NodeKind.LeafNode) {
			throw IllegalAccessException("trying to add child into LeafNode")
		}
		
		if(parent.children.isNotEmpty()) {
			consumeImplicitNodeIfApplicable()
		}
	}
	
	private fun enterGroup(group: CurrentGroup) {
		groups.addLast(group)
		if(group.kind == CstParseContext.NodeKind.Discardable) {
			discardableGroups.addLast(group)
		}
	}
	
	@CstParseContext.InternalApi
	override fun beginChildNode(
		kind: CstParseContext.NodeKind,
		info: CstNodeInfo<*>?,
	): CstParseContext {
		val parent = current
		beforeEnterGroup(parent, kind, info)
		
		val child = CurrentGroup(
			kind = kind,
			nodeInfo = info,
			codeSnapshot = if(kind == CstParseContext.NodeKind.Discardable || kind == CstParseContext.NodeKind.Peek) {
				c.createSnapshot()
			} else {
				null
			},
			flags = following?.flags ?: CurrentGroup.Flags(),
			beginIndex = c.offset,
			debugInfo = CurrentDebugInfo(
				name = meaningfulStackName(1),
				parsingStartedAtNs = System.nanoTime(),
				implicitLevel = if(insideConsumeImplicitNode) {
					parent.debugInfo.implicitLevel + 1
				} else {
					0
				},
			),
		)
		if(parent.flags.childrenAreDetached) {
			child.flags.detached = CurrentGroup.Detach(from = parent)
		}
		
		following = null
		enterGroup(child)
		
		return this
	}
	
	
	private fun leaveGroup(): CurrentGroup {
		val removed = groups.removeLast()
		if(removed == discardableGroups.lastOrNull()) {
			discardableGroups.removeLast()
		}
		return removed
	}
	
	private fun afterLeaveGroup(parent: CurrentGroup, group: Group) {
		// if(group.kind.node) {
		// 	parent.youCanInsertImplicitNode = true
		// }
		
		consumeImplicitNodeIfApplicable()
		
		parent.debugInfo.entries += DebugEntry.Child(group)
		
		if(groups.size == 1 && debugEnabled) {
			group.dump(1, this)
			System.out.flush()
		}
	}
	
	@CstParseContext.InternalApi
	override fun <Node : CstNode> endChildNode(childContext: CstParseContext, node: Node): Node {
		val child = leaveGroup()
		val parent = current
		
		if(child.kind == CstParseContext.NodeKind.Peek) {
			c.restoreToSnapshot(child.codeSnapshot ?: error("no codeSnapshot"))
			
			// Peek nodes are not assigned a tree
			return node
		} else {
			child.codeSnapshot?.discard()
		}
		
		val group = Group(
			kind = child.kind,
			children = child.children,
			node = node,
			nodeInfo = child.nodeInfo,
			flags = child.flags,
			span = child.beginIndex until c.offset,
			debugInfo = DebugInfo(
				name = meaningfulStackName(1),
				nestedLevel = groups.size,
				entries = child.debugInfo.entries,
				parsingDurationNs = System.nanoTime() - child.debugInfo.parsingStartedAtNs,
				implicitLevel = child.debugInfo.implicitLevel,
				attachedTo = parent,
			),
			error = null,
		)
		
		if(child.flags.detached == null) { // not detached
			parent.children += group
		}
		if(child.flags.preventDiscardHint) {
			val discardable = closestDiscardable
			if(discardable != null) {
				discardable.flags.selfDiscardPrevented = true
			}
		}
		node.attachTree(group)
		
		// Validation: is all nodes attached?
		val cstNode = CstNode::class.java
		val nodeClass = node::class.java
		for(field in nodeClass.declaredFields) {
			field.isAccessible = true
			
			if(!cstNode.isAssignableFrom(field.type)) continue
			val value = field.get(node) as CstNode
			val sub = value.tree as Group
			if(sub.debugInfo.attachedTo != child) {
				debug {
					Color.red(
						"attach not match for ${sub.debugInfo.name} " +
							"in ${nodeClass.simpleName}.${field.name}; attachedTo=${sub.debugInfo.attachedTo.debugInfo.name}",
					)
				}
			}
			if(sub.debugInfo.fieldName != null) {
				debug { Color.red("multiple attachment: previous=${sub.debugInfo.fieldName}, current=${field.name}") }
			}
			sub.debugInfo.fieldName = field.name
		}
		
		afterLeaveGroup(parent, group)
		
		return node
	}
	
	@CstParseContext.InternalApi
	override fun <Node : CstNode> skipChildNode(): Node = throw NotImplementedError()
	
	@CstParseContext.InternalApi
	override fun <Node : CstNode> endChildNodeWithError(
		childContext: CstParseContext,
		throwable: Throwable?,
	): Node? {
		assert(this === childContext)
		
		val child = leaveGroup()
		if(groups.isEmpty()) {
			throw IllegalStateException("groups stack is empty", throwable)
		}
		val parent = current
		if(child.codeSnapshot != null) {
			c.restoreToSnapshot(child.codeSnapshot)
		} else if(child.beginIndex != c.offset) {
			if(throwable == null) {
				debug {
					Color.red("endChildNodeWithError: do not call endChildNodeWithError out of discardableNode.") +
						" name=${meaningfulStackName(1)}"
				}
			}
			debug("endChildNodeWithError: could not restore code state; may cause inconsistency")
		}
		
		val errorGroup = Group(
			kind = child.kind,
			children = child.children,
			node = CstNode.dummyNode(),
			nodeInfo = child.nodeInfo,
			flags = child.flags,
			span = child.beginIndex until c.offset,
			debugInfo = DebugInfo(
				name = meaningfulStackName(1),
				nestedLevel = groups.size,
				entries = child.debugInfo.entries,
				parsingDurationNs = System.nanoTime() - child.debugInfo.parsingStartedAtNs,
				implicitLevel = child.debugInfo.implicitLevel,
				attachedTo = parent,
			),
			error = Group.GroupError(throwable = throwable),
		)
		
		afterLeaveGroup(parent, errorGroup)
		
		if(child.flags.selfDiscardPrevented) {
			parseError("discard prevented")
		}
		
		return null
	}
	
	override val lastEndError: Throwable? get() = null
	
	override fun provideRestartBlock(block: CstParseContext.() -> CstNode) {}
	
	override fun provideNodeHintToCurrent(hint: CstParseContext.NodeHint.ToCurrent) {
		debug("provideNodeHintToCurrent($hint)")
		when(hint) {
			CstParseContext.NodeHint.AllowCodeSourceAccess -> current.flags.allowCodeAccess = true
			is CstParseContext.NodeHint.ContextLocal<*> -> Unit
		}
	}
	
	override fun provideNodeHintToFollowing(hint: CstParseContext.NodeHint.ToFollowing) {
		debug("provideNodeHintToFollowing($hint)")
		val following = this.following ?: Following().also { this.following = it }
		when(hint) {
			CstParseContext.NodeHint.Vital -> following.flags.vitalHint = true
			CstParseContext.NodeHint.PreventDiscard -> following.flags.preventDiscardHint = true
		}
	}
	
	override fun markChildrenAsDetached(detached: Boolean) {
		current.flags.childrenAreDetached = detached
	}
	
	override fun markCurrentAsDetached() {
		current.flags.detached = CurrentGroup.Detach(parentOfCurrent)
	}
	
	override fun <Node : CstNode> insertChildNode(node: Node): Node {
		consumeImplicitNodeIfApplicable()
		
		// The cases where this was called are:
		// - attach previously detached node
		
		val tree = node.tree
		if(tree !is Group) error("unknown tree")
		
		val current = current
		val group = tree.copy(
			debugInfo = tree.debugInfo.copy(
				name = meaningfulStackName(1, or = tree.debugInfo.name),
				attachedTo = current,
			),
			flags = tree.flags.copy(reAttached = true),
		)
		current.children += group
		current.debugInfo.entries += DebugEntry.Child(group)
		
		if(tree.span.first < current.beginIndex) {
			current.beginIndex = tree.span.first
		}
		
		return node
	}
	
	override fun pushDiagnostic(diagnostic: Diagnostic) {
		println(diagnostic)
	}
}

fun main() {
	fun parse(code: String, init: TextParseContext.() -> Unit) {
		println("Input code: $code")
		val duration = measureTime {
			TextParseContext(code).apply {
				cstImplicitNodeOrNull() // this is not automatic; implicit nodes are 'only between nodes'
				init()
				cstImplicitNodeOrNull()
			}
		}
		println("== parse took $duration")
		println()
	}
	
	parse("fun, class, object") {
		disableAdjacentImplicitNode()
		cstCommaSeparatedList(CstKeyword) { cstKeyword() }
	}
	
	parse(
		"""println("hello, ${'$'}name! ${'$'}{1 + 2}", a = 123 + 0x14)""".trimIndent(),
	) {
		cstExpression()
	}
}
