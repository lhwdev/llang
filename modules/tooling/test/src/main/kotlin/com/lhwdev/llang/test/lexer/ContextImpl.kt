package com.lhwdev.llang.test.lexer

import com.lhwdev.llang.cst.structure.CstLocalTreeProvider
import com.lhwdev.llang.cst.structure.CstNode
import com.lhwdev.llang.cst.structure.CstTreeProvider
import com.lhwdev.llang.cst.structure.LocalCstTreeProvider
import com.lhwdev.llang.cst.tree.CstTreeNode
import com.lhwdev.llang.diagnostic.Diagnostic
import com.lhwdev.llang.parser.CstCodeSource
import com.lhwdev.llang.parser.CstParseContext
import com.lhwdev.llang.parser.core.cstImplicitNodeOrNull
import com.lhwdev.llang.parsing.parseError
import com.lhwdev.llang.parsing.util.ParseException
import com.lhwdev.llang.token.Token
import com.lhwdev.llang.token.TokenImpl
import com.lhwdev.llang.token.TokenKind
import com.lhwdev.llang.tokenizer.source.CodeSequence
import com.lhwdev.llang.tokenizer.source.token
import com.lhwdev.utils.platform.withValue
import kotlin.math.min

private abstract class BasicCharSequence : CharSequence {
	override fun equals(other: Any?): Boolean =
		other === this || (other is CharSequence && contentEquals(other))
	
	override fun hashCode(): Int = toString().hashCode()
}

class TextCodeSource(
	private val parent: TextParseContext,
	val text: String,
	var offset: Int = 0,
	var spanStart: Int = 0,
	private val debugName: String = "Lexer",
) : CstCodeSource {
	private var spanLocation: StackLocation? = null
	
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
	
	override val next: CodeSequence = object : CodeSequence, BasicCharSequence() {
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
	override val currentSpan: CharSequence = object : BasicCharSequence() {
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
		if(offset == spanStart) {
			spanLocation = stackLocation(n = 1)
		}
		offset += count
		
		if(offset > text.length) error("holy wtf")
	}
	
	fun acceptToken(token: Token) {
		debug("acceptToken $token, remaining=$next")
		// TODO: retain token reference! this is stub implementation.
		//       result token should be === [token].
		token(token.kind) {
			advance(token.code.length)
			require(currentSpan == token.code) {
				"expected same token as \"${token.code}\"; got \"$currentSpan\""
			}
		}
	}
	
	fun acceptSpan(span: Group, depth: Int = 0) {
		debug(
			"  ".repeat(/*span.debugInfo.nestedLevel*/ depth) +
				"acceptSpan ${span.debugInfo.name} " +
				"'${parent.c.text.slice(span.span)}' " +
				"next='${next.substring(0, min(next.length, 6))}'",
		)
		if(span.kind == CstParseContext.NodeKind.LeafNode) {
			debug("  ".repeat(span.debugInfo.nestedLevel) + "acceptTokens ${span.tokens.joinToString { it.code }}")
			span.tokens.forEach { acceptToken(it) }
		} else {
			for(child in span.children) {
				acceptSpan(child, depth = depth + 1)
			}
		}
	}
	
	private fun tokenTrace() = Throwable().stackTrace
		.drop(2)
		.takeWhile { it.className.startsWith("com.lhwdev.llang.tokenizer") }
		.filter { "$" !in it.methodName }
		.map {
			val name = it.methodName
			when {
				name.startsWith("parse") -> name.drop(5)
				else -> name
			}
		}
		.mapIndexed { index, name -> if(index == 0) Color.yellow(name) else Color.lightGray(name) }
	
	override fun buildToken(kind: TokenKind): Token {
		val code = currentSpan.toString()
		val trace = if(debugTraceStack) tokenTrace() else listOf(Color.yellow("?"))
		
		parent.debug(
			10,
			"${Color.blue("$debugName token")} " + "" +
				"${trace.joinToString(separator = Color.gray("<-"))} " +
				"$kind ${Color.green(escape(code))}",
			// " remaining=${Color.green(escape(next.take(10).toString()))}",
		)
		spanStart = offset
		spanLocation = null
		val token = TokenImpl(kind, code)
		parent.current.tokens += token
		return token
	}
	
	override fun hiddenDebugCommands(command: String, vararg args: Any?): Any = when(command) {
		"requireEmptyErrorMessage" -> parseError("non-empty span (first advance() called in $spanLocation)")
		else -> error("unknown command $command")
	}
	
	
	override fun resetToSpanStart() {
		offset = spanStart
	}
	
	override fun parseError(exception: ParseException): Nothing {
		exception.extraInfo = spanLocation
		throw exception
	}
}

class Following(
	var flags: CurrentGroup.Flags = CurrentGroup.Flags(),
)

interface SimpleGroupMetadata {
	val kind: CstParseContext.NodeKind
	val beginIndex: Int
}

data class PreBeginGroup(
	override val kind: CstParseContext.NodeKind,
	override val beginIndex: Int,
) : SimpleGroupMetadata

data class CurrentGroup(
	val depth: Int,
	override var kind: CstParseContext.NodeKind,
	var codeSnapshot: TextCodeSource.Snapshot?,
	val children: MutableList<Group> = mutableListOf(),
	val flags: Flags,
	override var beginIndex: Int,
	val debugInfo: CurrentDebugInfo,
	var youCanInsertImplicitNode: Boolean = false,
) : CstLocalTreeProvider, SimpleGroupMetadata {
	companion object {
		val Root = CurrentGroup(
			depth = 0,
			kind = CstParseContext.NodeKind.Node,
			codeSnapshot = null,
			flags = Flags(),
			beginIndex = 0,
			debugInfo = CurrentDebugInfo(
				name = "<root-node>",
				parsingStartedAtNs = 0,
				implicitLevel = 0,
				location = StackLocation.Stub,
			),
		).also {
			it.flags.consumptionScope = it
			it.parent = it
		}
	}
	
	
	lateinit var parent: CurrentGroup
	
	val tokens = mutableListOf<Token>()
	
	var group: Group? = null
	
	override val tree: CstTreeNode
		get() = group!!
	
	
	fun descriptionToString(): String =
		group?.descriptionToString() ?: "${debugInfo.name}(${escape(contentToString())})"
	
	private fun contentToString(): String =
		tokens.joinToString(separator = "")
	
	
	class Flags(
		// var consumptionScope: ConsumptionScope? = null,
		var reAttached: Boolean = false,
		var detached: Detach? = null,
		var childrenDetached: ChildrenDetach? = null,
		var implicit: Boolean = false,
		var disableAdjacentImplicitNode: Boolean = false,
		var vitalHint: Boolean = false,
		var preventDiscardHint: Boolean = false,
		var selfDiscardPrevented: Boolean = false,
	) {
		lateinit var consumptionScope: CurrentGroup
		
		fun copy(
			consumptionScope: CurrentGroup = this.consumptionScope,
			reAttached: Boolean = this.reAttached,
			detached: Detach? = this.detached,
			childrenDetached: ChildrenDetach? = this.childrenDetached,
			implicit: Boolean = this.implicit,
			disableAdjacentImplicitNode: Boolean = this.disableAdjacentImplicitNode,
			vitalHint: Boolean = this.vitalHint,
			preventDiscardHint: Boolean = this.preventDiscardHint,
			selfDiscardPrevented: Boolean = this.selfDiscardPrevented,
		): Flags = Flags(
			reAttached, detached,
			childrenDetached,
			implicit, disableAdjacentImplicitNode,
			vitalHint, preventDiscardHint,
			selfDiscardPrevented,
		).also {
			it.consumptionScope = consumptionScope
		}
		
		override fun toString(): String = """
			Flags(
				consumptionScope = ${consumptionScope.descriptionToString()},
				attachment = (detached = $detached, reAttached = $reAttached),
				childrenDetached = $childrenDetached,
				hint = (vital = $vitalHint, preventDiscard = $preventDiscardHint),
				selfDiscardPrevented = $selfDiscardPrevented,
				implicit = $implicit, disableAdjacentImplicitNode = $disableAdjacentImplicitNode,
			)
		""".trimIndent()
	}
	
	data class Detach(val from: CurrentGroup) {
		override fun toString(): String = "Detach(from = ${from.descriptionToString()})"
	}
	
	sealed class ChildrenDetach {
		data object Simple : ChildrenDetach()
		
		data class Peek(val snapshot: TextCodeSource.Snapshot) : ChildrenDetach()
	}
}

fun CurrentGroup.hasParent(group: CurrentGroup): Boolean {
	var target = parent
	while(true) {
		if(target == group) return true
		
		val newTarget = target.parent
		if(newTarget == target) return false
		target = newTarget
	}
}


interface ConsumptionScope {
	val boundTo: CurrentGroup
	
	// TODO: implementations included here for proper abstraction
	
	class Discardable(override val boundTo: CurrentGroup) : ConsumptionScope
	
	
	class Peek(override val boundTo: CurrentGroup) : ConsumptionScope
}


data class CurrentDebugInfo(
	val name: String,
	val parsingStartedAtNs: Long,
	val implicitLevel: Int,
	val location: StackLocation,
	val entries: MutableList<DebugEntry> = mutableListOf(),
)

sealed class DebugEntry {
	data class Line(val line: String, val level: Int) : DebugEntry()
	data class Child(val group: Group) : DebugEntry()
}

private object Debug {
	fun indent(level: Int) = "  ".repeat(level - 1)
}

data class Group(
	val current: CurrentGroup,
	override val kind: CstParseContext.NodeKind,
	val parent: CurrentGroup?,
	val children: MutableList<Group>,
	val node: CstNode,
	val flags: CurrentGroup.Flags,
	val span: IntRange,
	val tokens: List<Token>,
	val debugInfo: DebugInfo,
	val error: GroupError?,
) : CstTreeNode, SimpleGroupMetadata {
	class GroupError(
		val throwable: Throwable?,
	)
	
	data class DumpContext(
		val insideAttached: Boolean = false,
		val ignoreDetached: Boolean = true,
	) {
		companion object {
			val DumpDefault = DumpContext(ignoreDetached = false)
			
			val TreeDefault = DumpContext(ignoreDetached = true)
		}
	}
	
	
	val depth: Int
		get() = current.depth
	
	override val beginIndex: Int
		get() = span.first
	
	override fun hashCode(): Int = 0
	
	override fun toString(): String {
		val attachedTo = debugInfo.attachedTo
		if(attachedTo != null && this in attachedTo.children) {
			return "recursive? attachedTo=${attachedTo.debugInfo.name}, this=${debugInfo.name}"
		}
		
		return super.toString()
	}
	
	fun descriptionToString(): String = "${debugInfo.name}(${escape(contentToString())})"
	
	
	private var contentString: String? = null
	
	fun contentToString(scope: CurrentGroup = current): String = if(scope == current) {
		contentString ?: contentToStringImpl(scope).also { contentString = it }
	} else {
		contentToStringImpl(scope)
	}
	
	private fun contentToStringImpl(scope: CurrentGroup): String = buildString {
		if(flags.consumptionScope.hasParent(scope)) return ""
		if(kind == CstParseContext.NodeKind.LeafNode) {
			for(token in tokens) append(token.code)
		} else {
			for(child in children) {
				append(child.contentToString(scope))
			}
		}
	}
	
	
	fun dump(
		context: TextParseContext,
		depth: Int = 1,
		dump: DumpContext = DumpContext.DumpDefault,
	) {
		// fun formatError(throwable: Throwable) =
		// 	"${throwable::class.java.simpleName}${if(throwable.message != null) ": ${throwable.message}" else ""}"
		
		if(dump.insideAttached) {
			if(error != null || flags.detached != null) {
				return
			}
		}
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
			if(dump.ignoreDetached) return
			kind = "Detached $kind"
		}
		
		
		var name = debugInfo.name
		var functionName: String? = null
		var infoName =
			node.info::class.java.name.substringAfterLast('.').substringBeforeLast('$')
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
					infoName = ""
				}
			}
		}
		name = Color.bold(name)
		val fieldName = debugInfo.fieldName
		if(fieldName != null) {
			name = "$fieldName=$name"
		}
		
		val tookValid = !dump.insideAttached
		val tokenContent = contentToString()
		val spanContent = context.c.text.slice(span)
		val content = if(tokenContent == spanContent) {
			escape(tokenContent)
		} else {
			"⚠️fromToken(${escape(tokenContent)}) vs fromSpan(${escape(spanContent)})"
		}
		
		if(error != null) {
			println(
				Debug.indent(depth) +
					(if(error.throwable != null) Color.red else Color.lightGray)(kind) +
					"(${if(error.throwable != null) "error" else "discarded"}) " +
					"$name " +
					// (if(error.throwable != null) "error=${Color.lightGray("'${formatError(error.throwable)}'")} " else "") +
					(if(functionName != null) "function=${Color.green(functionName)} " else "") +
					(if(infoName.isNotEmpty()) "info=${Color.green(infoName)} " else "") +
					"span=${Color.lightGreen(content)} " +
					"consumptionScope=${flags.consumptionScope.debugInfo.name} " +
					(if(tookValid) "took=${Color.green("${debugInfo.parsingDurationNs / 10000 / 100f}ms")}" else ""),
			)
			if(error.throwable == null) return
		} else {
			println(
				"${Debug.indent(depth)}${
					(if(debugInfo.implicitLevel != 0) Color.dimCyan else Color.cyan)(kind)
				}" +
					" $name " +
					(if(functionName != null) "function=${Color.green(functionName)} " else "") +
					(if(infoName.isNotEmpty()) "info=${Color.green(infoName)} " else "") +
					"span=${Color.lightGreen(content)} " +
					"consumptionScope=${flags.consumptionScope.debugInfo.name} " +
					(if(tookValid) "took=${Color.green("${debugInfo.parsingDurationNs / 10000 / 100f}ms")}" else ""),
			)
		}
		if(debugInfo.implicitLevel == 1) return
		
		val indent = Debug.indent(depth + 1)
		var childDump = dump
		if(flags.reAttached) {
			childDump = childDump.copy(insideAttached = true)
		}
		
		val subIndent = "$indent  "
		for(entry in debugInfo.entries) {
			when(entry) {
				is DebugEntry.Line -> {
					if(childDump.insideAttached && entry.level < 5) continue
					println("${indent}${entry.line.replace("\n", subIndent)}")
				}
				
				is DebugEntry.Child -> {
					entry.group.dump(context, depth + 1, childDump)
				}
			}
		}
	}
	
	fun dumpTree(
		context: TextParseContext,
		depth: Int = 1,
		dump: DumpContext = DumpContext.TreeDefault,
	) {
		if(dump.insideAttached) {
			if(error != null || flags.detached != null) {
				return
			}
		}
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
			// if(dump.ignoreDetached) return
			kind = "Detached $kind"
		}
		
		
		var name = debugInfo.name
		var functionName: String? = null
		var infoName =
			node.info::class.java.name.substringAfterLast('.').substringBeforeLast('$')
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
					infoName = ""
				}
			}
		}
		name = Color.bold(name)
		val fieldName = debugInfo.fieldName
		if(fieldName != null) {
			name = "$fieldName=$name"
		}
		
		val tookValid = !dump.insideAttached
		
		if(error != null) {
			println(
				Debug.indent(depth) +
					(if(error.throwable != null) Color.red else Color.lightGray)(kind) +
					"(${if(error.throwable != null) "error" else "discarded"}) " +
					"$name " +
					// (if(error.throwable != null) "error=${Color.lightGray("'${formatError(error.throwable)}'")} " else "") +
					(if(functionName != null) "function=${Color.green(functionName)} " else "") +
					(if(infoName.isNotEmpty()) "info=${Color.green(infoName)} " else "") +
					"span=${Color.lightGreen(escape(context.c.text.slice(span)))} " +
					(if(tookValid) "took=${Color.green("${debugInfo.parsingDurationNs / 10000 / 100f}ms")}" else ""),
			)
			if(error.throwable == null) return
		} else {
			println(
				"${Debug.indent(depth)}${
					(if(debugInfo.implicitLevel != 0) Color.dimCyan else Color.cyan)(kind)
				}" +
					" $name " +
					(if(functionName != null) "function=${Color.green(functionName)} " else "") +
					(if(infoName.isNotEmpty()) "info=${Color.green(infoName)} " else "") +
					"span=${Color.lightGreen(escape(context.c.text.slice(span)))} " +
					(if(tookValid) "took=${Color.green("${debugInfo.parsingDurationNs / 10000 / 100f}ms")}" else ""),
			)
		}
		if(debugInfo.implicitLevel == 1) return
		
		val indent = Debug.indent(depth + 1)
		var childDump = dump
		if(flags.reAttached) {
			childDump = childDump.copy(insideAttached = true)
		}
		
		for(child in children) {
			child.dumpTree(context, depth + 1, childDump)
		}
	}
}

data class DebugInfo(
	val name: String,
	val nestedLevel: Int,
	val entries: List<DebugEntry>,
	val parsingDurationNs: Long,
	val implicitLevel: Int,
	var attachedTo: CurrentGroup?,
	val location: StackLocation,
	val attachName: String? = null,
	var fieldName: String? = null,
)

class TextParseContext(text: String) : CstParseContext {
	override val code: CstCodeSource
		get() {
			val current = current
			if(current.kind == CstParseContext.NodeKind.LeafNode && current.children.isEmpty()) {
				return c
			} else {
				throw IllegalAccessException("do not access code in normal node")
			}
		}
	
	@CstParseContext.InternalApi
	override val dangerousCode: CstCodeSource
		get() = c
	
	internal val c = TextCodeSource(this, text)
	
	val groups = ArrayDeque<CurrentGroup>().also {
		it += CurrentGroup.Root
	}
	private val discardableGroups = ArrayDeque<CurrentGroup>()
	
	private var following: Following? = null
	val current get() = groups.last()
	val parentOfCurrent get() = groups[groups.size - 2]
	val closestDiscardable get() = discardableGroups.lastOrNull()
	
	var lastGroupForDebug: Group? = null
	
	override val debugEnabled: Boolean = true
	
	val immediateDebugEnabled: Boolean = true
	
	override fun debug(line: String) {
		debug(level = 2, line)
	}
	
	fun debug(level: Int, line: String) {
		if(immediateDebugEnabled) println("  ".repeat(groups.size - 1) + line)
		if(debugEnabled) current.debugInfo.entries += DebugEntry.Line(line, level)
	}
	
	inline fun debug(level: Int, block: () -> String) {
		if(debugEnabled) debug(level, block())
	}
	
	override fun disableAdjacentImplicitNode() {
		current.flags.disableAdjacentImplicitNode = true
	}
	
	private var insideConsumeImplicitNode = false
	private fun consumeImplicitNodeIfApplicable(parent: CurrentGroup, child: SimpleGroupMetadata) {
		if(insideConsumeImplicitNode) return
		if(current.flags.disableAdjacentImplicitNode) return
		
		val current = current
		if(!current.kind.isNode || current.kind == CstParseContext.NodeKind.LeafNode) {
			return
		}
		
		insideConsumeImplicitNode = true
		try {
			cstImplicitNodeOrNull()
		} finally {
			insideConsumeImplicitNode = false
		}
	}
	
	@CstParseContext.InternalApi
	override val alwaysRequireNodeInfo: Boolean
		get() = true
	
	private fun beforeEnterGroup(parent: CurrentGroup, pre: SimpleGroupMetadata) {
		// Implicit node
		if(parent.youCanInsertImplicitNode) {
			consumeImplicitNodeIfApplicable(parent, pre)
			parent.youCanInsertImplicitNode = false
		}
	}
	
	private fun enterGroup(group: CurrentGroup) {
		groups.addLast(group)
		if(group.kind == CstParseContext.NodeKind.Discardable) {
			discardableGroups.addLast(group)
		}
	}
	
	@CstParseContext.InternalApi
	override fun beginChildNode(kind: CstParseContext.NodeKind): CstParseContext? = try {
		beginChildNodeInternal(kind)
	} catch(throwable: Throwable) {
		println("ERROR during beginChildNode:")
		println(throwable)
		throw throwable
	}
	
	@CstParseContext.InternalApi
	private fun beginChildNodeInternal(kind: CstParseContext.NodeKind): CstParseContext {
		val parent = current
		val pre = PreBeginGroup(kind = kind, beginIndex = c.offset)
		beforeEnterGroup(parent, pre)
		
		if(parent.kind == CstParseContext.NodeKind.LeafNode) {
			throw IllegalAccessException("trying to add child into LeafNode")
		}
		val location = stackLocation(n = 3)
		
		if(immediateDebugEnabled) println("  ".repeat(groups.size - 1) + Color.cyan(kind.name) + " ${location.meaningfulStackName()} ")
		
		
		val flags = following?.flags ?: CurrentGroup.Flags()
		flags.implicit = insideConsumeImplicitNode
		if(parent.flags.childrenDetached != null) {
			flags.detached = CurrentGroup.Detach(from = parent)
		}
		if(kind == CstParseContext.NodeKind.Peek) {
			flags.childrenDetached = CurrentGroup.ChildrenDetach.Simple
		}
		
		val child = CurrentGroup(
			depth = parent.depth + 1,
			kind = kind,
			codeSnapshot = if(kind == CstParseContext.NodeKind.Discardable || kind == CstParseContext.NodeKind.Peek) {
				c.createSnapshot()
			} else {
				null
			},
			flags = flags,
			beginIndex = c.offset,
			debugInfo = CurrentDebugInfo(
				name = location.meaningfulStackName(),
				parsingStartedAtNs = System.nanoTime(),
				implicitLevel = if(insideConsumeImplicitNode) {
					parent.debugInfo.implicitLevel + 1
				} else {
					0
				},
				location = location,
			),
		)
		
		flags.consumptionScope = when {
			kind == CstParseContext.NodeKind.Peek -> child
			parent.flags.childrenDetached is CurrentGroup.ChildrenDetach.Peek -> parent
			else -> parent.flags.consumptionScope
		}
		
		child.parent = parent
		
		following = null
		enterGroup(child)
		
		return this
	}
	
	private fun beginChildNodeStub(
		kind: CstParseContext.NodeKind,
		existing: Group,
	): CstParseContext {
		val parent = current
		val pre = PreBeginGroup(kind = kind, beginIndex = c.offset)
		beforeEnterGroup(parent, pre)
		
		if(parent.kind == CstParseContext.NodeKind.LeafNode) {
			if(kind != CstParseContext.NodeKind.LeafNode) throw IllegalAccessException(
				"trying to add child ${existing.debugInfo.name}" +
					"('${existing.contentToString()}') into LeafNode",
			)
		}
		
		val debugInfo = existing.current.debugInfo
		val child = existing.current.copy(
			children = mutableListOf(),
			flags = existing.flags.copy(
				detached = null,
				reAttached = false,
			),
			debugInfo = debugInfo.copy(name = "<stub>${debugInfo.name}"),
		)
		
		child.flags.consumptionScope = when {
			kind == CstParseContext.NodeKind.Peek -> child
			parent.flags.childrenDetached is CurrentGroup.ChildrenDetach.Peek -> parent
			else -> parent.flags.consumptionScope
		}
		child.parent = parent
		
		following = null
		enterGroup(child)
		
		return this
	}
	
	private fun ensureSnapshotExists(): TextCodeSource.Snapshot =
		current.codeSnapshot ?: createSnapshot().also { current.codeSnapshot = it }
	
	private fun createSnapshot(): TextCodeSource.Snapshot {
		val current = current
		check(current.children.isEmpty() && current.tokens.isEmpty()) {
			"call createSnapshot before adding children / tokens"
		}
		return c.createSnapshot()
	}
	
	
	private fun leaveGroup(): CurrentGroup {
		val removed = groups.removeLast()
		if(removed == discardableGroups.lastOrNull()) {
			discardableGroups.removeLast()
		}
		return removed
	}
	
	private fun afterLeaveGroup(parent: CurrentGroup, group: Group, previousSelf: CurrentGroup?) {
		if(group.kind.isNode && group.kind != CstParseContext.NodeKind.Peek) {
			parent.youCanInsertImplicitNode = true
		}
		
		if(previousSelf != null) {
			previousSelf.group = group
		}
		
		if(parent.kind == CstParseContext.NodeKind.LeafNode) {
			parent.tokens += group.tokens
		}
	}
	
	private fun afterLeaveGroupIncludingPeek(parent: CurrentGroup, group: Group) {
		parent.debugInfo.entries += DebugEntry.Child(group)
		
		if(groups.size == 1 && debugEnabled) {
			group.dump(this, 1)
			System.out.flush()
		}
	}
	
	@CstParseContext.InternalApi
	override fun <Node : CstNode> endChildNode(
		childContext: CstParseContext,
		node: Node,
	): Node = try {
		endChildNodeInternal(childContext, node)
	} catch(throwable: Throwable) {
		println("ERROR during endChildNode:")
		println(throwable)
		throw throwable
	}
	
	@CstParseContext.InternalApi
	fun <Node : CstNode> endChildNodeInternal(childContext: CstParseContext, node: Node): Node {
		check(childContext == this)
		
		current.flags.childrenDetached?.let { endChildrenAsDetachedInternal(it) }
		
		val child = leaveGroup()
		val parent = current
		val location = stackLocation(n = 3)
		
		if(child.kind == CstParseContext.NodeKind.Peek) {
			c.restoreToSnapshot(child.codeSnapshot ?: error("no codeSnapshot on Peek node"))
			
			val group = Group(
				current = child,
				kind = child.kind,
				parent = parent,
				children = child.children,
				node = node,
				flags = child.flags,
				span = child.beginIndex until c.offset,
				tokens = child.tokens,
				debugInfo = DebugInfo(
					name = location.meaningfulStackName(),
					nestedLevel = groups.size,
					entries = child.debugInfo.entries,
					parsingDurationNs = System.nanoTime() - child.debugInfo.parsingStartedAtNs,
					implicitLevel = child.debugInfo.implicitLevel,
					attachedTo = parent.takeIf { child.flags.detached == null },
					location = location,
				),
				error = null,
			)
			afterLeaveGroupIncludingPeek(parent, group)
			
			// Peek nodes are not assigned a tree
			return node
		} else {
			child.codeSnapshot?.discard()
		}
		
		val group = Group(
			current = child,
			kind = child.kind,
			parent = parent,
			children = child.children,
			node = node,
			flags = child.flags,
			span = child.beginIndex until c.offset,
			tokens = child.tokens,
			debugInfo = DebugInfo(
				name = location.meaningfulStackName(),
				nestedLevel = groups.size,
				entries = child.debugInfo.entries,
				parsingDurationNs = System.nanoTime() - child.debugInfo.parsingStartedAtNs,
				implicitLevel = child.debugInfo.implicitLevel,
				attachedTo = parent.takeIf { child.flags.detached == null },
				location = location,
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
		
		afterLeaveGroup(parent, group, child)
		afterLeaveGroupIncludingPeek(parent, group)
		
		// if(node.tree !== group) {
		// 	throw IllegalStateException("Expected node to be attached to current tree; expected=${group.debugInfo.name}, actual=${(node.tree as Group).debugInfo.name}")
		// }
		
		// if(child.kind == CstParseContext.NodeKind.Peek) {
		// 	c.restoreToSnapshot(child.codeSnapshot ?: error("no codeSnapshot on Peek node"))
		// }
		
		// Validation: is all nodes attached?
		val cstNode = CstNode::class.java
		val nodeClass = node::class.java
		for(field in nodeClass.declaredFields) {
			field.isAccessible = true
			
			if(!cstNode.isAssignableFrom(field.type)) continue
			val value = field.get(node) as CstNode
			val sub = value.tree as Group
			
			if(sub.debugInfo.attachedTo != child) {
				debug(1) {
					Color.red(
						"attach not match for ${sub.debugInfo.name} " +
							"in ${nodeClass.simpleName}.${field.name}; attachedTo=${sub.debugInfo.attachedTo?.debugInfo?.name}",
					)
				}
			}
			
			val fieldName = field.declaringClass.simpleName + "." + field.name
			if(sub.debugInfo.fieldName != null) {
				debug(1) { Color.red("multiple attachment: previous=${sub.debugInfo.fieldName}, current=${fieldName}") }
			}
			sub.debugInfo.fieldName = fieldName
		}
		
		return node
	}
	
	private fun <Node : CstNode> endChildNodeStub(
		childContext: CstParseContext,
		node: Node,
		existing: Group,
	): Node {
		check(childContext == this)
		
		val child = leaveGroup()
		val parent = current
		
		val group = existing.copy(
			children = child.children,
			debugInfo = existing.debugInfo.copy(name = "<stub>${existing.debugInfo.name}"),
		)
		
		if(child.flags.detached == null) {
			parent.children += group
		}
		if(child.flags.preventDiscardHint) {
			val discardable = closestDiscardable
			if(discardable != null) {
				discardable.flags.selfDiscardPrevented = true
			}
		}
		
		afterLeaveGroup(parent, group, child)
		afterLeaveGroupIncludingPeek(parent, group)
		
		return node
	}
	
	@CstParseContext.InternalApi
	override fun <Node : CstNode> skipChildNode(): Node = throw NotImplementedError()
	
	@CstParseContext.InternalApi
	override fun <Node : CstNode> endChildNodeWithError(
		childContext: CstParseContext,
		throwable: Throwable?,
	): Node? = try {
		endChildNodeWithErrorInternal(childContext, throwable)
	} catch(th: Throwable) {
		println("ERROR during endChildNodeWithError:")
		println(th)
		throw throwable ?: th
	}
	
	@CstParseContext.InternalApi
	fun <Node : CstNode> endChildNodeWithErrorInternal(
		childContext: CstParseContext,
		throwable: Throwable?,
	): Node? {
		assert(this === childContext)
		
		val child = leaveGroup()
		if(groups.isEmpty()) {
			throw IllegalStateException("groups stack is empty", throwable)
		}
		val parent = current
		
		val location = stackLocation(n = 3)
		val errorGroup = Group(
			current = child,
			kind = child.kind,
			parent = parent,
			children = child.children,
			node = CstNode.dummyNode(),
			flags = child.flags,
			span = child.beginIndex until c.offset,
			tokens = child.tokens,
			debugInfo = DebugInfo(
				name = location.meaningfulStackName(),
				nestedLevel = groups.size,
				entries = child.debugInfo.entries,
				parsingDurationNs = System.nanoTime() - child.debugInfo.parsingStartedAtNs,
				implicitLevel = child.debugInfo.implicitLevel,
				attachedTo = parent.takeIf { child.flags.detached == null },
				location = location,
			),
			error = Group.GroupError(throwable = throwable),
		)
		
		afterLeaveGroup(parent, errorGroup, child)
		afterLeaveGroupIncludingPeek(parent, errorGroup)
		
		val snapshot = child.codeSnapshot
		if(snapshot != null) {
			c.restoreToSnapshot(snapshot)
		} else if(closestDiscardable != null) {
			// no-op
		} else if(child.beginIndex != c.offset) {
			if(throwable == null) {
				debug(1) {
					Color.red("endChildNodeWithError: do not call endChildNodeWithError out of discardableNode.") +
						" name=${location.meaningfulStackName()}"
				}
			}
			debug(
				1,
				Color.red(
					"endChildNodeWithError: could not restore code state; may cause inconsistency" +
						" (span=${c.text.substring(child.beginIndex, c.offset)})",
				),
			)
		}
		
		if(child.flags.selfDiscardPrevented) {
			parseError("discard prevented")
		}
		
		return null
	}
	
	fun <R> withRootGroup(block: TextParseContext.() -> R): R {
		require(groups.size == 1) { "should be called in root" }
		
		val provider = object : CstTreeProvider {
			override fun local(): CstLocalTreeProvider = current
		}
		
		return LocalCstTreeProvider.withValue(provider) {
			block()
		}
	}
	
	
	override val lastEndError: Throwable? = null
	
	override fun provideRestartBlock(block: CstParseContext.() -> CstNode) {}
	
	override fun provideNodeHintToCurrent(hint: CstParseContext.NodeHint.ToCurrent) {
		debug(1, "provideNodeHintToCurrent($hint)")
		when(hint) {
			is CstParseContext.NodeHint.ContextLocal<*> -> Unit
		}
	}
	
	override fun provideNodeHintToFollowing(hint: CstParseContext.NodeHint.ToFollowing) {
		debug(1, "provideNodeHintToFollowing($hint)")
		val following = this.following ?: Following().also { this.following = it }
		when(hint) {
			CstParseContext.NodeHint.Vital -> following.flags.vitalHint = true
			CstParseContext.NodeHint.PreventDiscard -> following.flags.preventDiscardHint = true
		}
	}
	
	override fun markChildrenAsDetached(peek: Boolean) {
		debug("markChildrenAsDetached(peek = $peek)")
		
		current.flags.childrenDetached = if(peek) {
			val snapshot = ensureSnapshotExists()
			CurrentGroup.ChildrenDetach.Peek(snapshot)
		} else {
			CurrentGroup.ChildrenDetach.Simple
		}
	}
	
	override fun endChildrenAsDetached() {
		debug("endChildrenAsDetached()")
		val previous = current.flags.childrenDetached
			?: throw IllegalStateException("Children are not already detached")
		
		endChildrenAsDetachedInternal(previous)
		
		current.flags.childrenDetached = null
	}
	
	private fun endChildrenAsDetachedInternal(detach: CurrentGroup.ChildrenDetach) {
		if(detach is CurrentGroup.ChildrenDetach.Peek) {
			c.restoreToSnapshot(detach.snapshot)
		}
	}
	
	override fun markCurrentAsDetached() {
		current.flags.detached = CurrentGroup.Detach(parentOfCurrent)
	}
	
	private fun <Node : CstNode> remoteChildNode(node: Node, block: (tree: Group) -> Node): Node {
		// The cases where this was called are:
		// - attach previously detached node
		
		val tree = node.tree
		if(tree !is Group) error("unknown tree")
		
		val parent = current
		beforeEnterGroup(parent, tree)
		
		if(parent.kind == CstParseContext.NodeKind.LeafNode) {
			if(tree.kind != CstParseContext.NodeKind.LeafNode) {
				// TODO: allow !kind.isNode nodes recursively
				throw IllegalStateException("cannot insert other node than leaf inside leaf")
			}
		}
		
		// ensureDetached
		val attachedTo = tree.debugInfo.attachedTo
		if(attachedTo != null) {
			fun throwError(): Nothing =
				throw IllegalStateException("trying to insert already attached node(node = $node, attachedTo = ${tree.debugInfo.attachedTo?.debugInfo?.name ?: "???"}) into ${parent.debugInfo.name}\nattached location=${tree.debugInfo.location}")
			
			var current: CurrentGroup = attachedTo
			while(true) {
				val group = current.group
				if(group == null) {
					throwError()
				} else {
					if(group.flags.detached != null) {
						// in case 'attached to A, but A was detached'
						break
					}
				}
				current = group.parent ?: throwError()
			}
		}
		
		if(tree.kind == CstParseContext.NodeKind.Peek) {
			throw IllegalStateException("Cannot insert Peek node into tree")
			
			// I don't know if this is right behavior...
			// acceptNode(tree)
		}
		
		val newNode = block(tree)
		
		val location = stackLocation(n = 2)
		val group = tree.copy(
			kind = @Suppress("KotlinConstantConditions") if(
				tree.kind == CstParseContext.NodeKind.Peek) {
				CstParseContext.NodeKind.Node
			} else {
				tree.kind
			},
			debugInfo = tree.debugInfo.copy(
				attachName = location.meaningfulStackName(or = tree.debugInfo.name),
				attachedTo = parent,
				location = location,
			),
			flags = tree.flags.copy(
				reAttached = true,
				detached = if(parent.flags.childrenDetached != null) {
					CurrentGroup.Detach(from = parent)
				} else {
					null
				},
			),
		)
		parent.children += group
		lastGroupForDebug = group
		
		afterLeaveGroup(parent, group, previousSelf = null)
		afterLeaveGroupIncludingPeek(parent, group)
		if(tree.span.first < parent.beginIndex) {
			parent.beginIndex = tree.span.first
		}
		
		return newNode
	}
	
	override fun <Node : CstNode> insertChildNode(node: Node): Node =
		remoteChildNode(node) { node }
	
	override fun <Node : CstNode> acceptChildNode(node: Node): Node =
		remoteChildNode(node) { tree ->
			// TODO: check
			// - check(!tree.flags.consumptionScope) { "node already consumed!" }
			acceptNode(tree)
			node
		}
	
	private fun acceptNode(tree: Group) {
		val parent = current
		val context = beginChildNodeStub(kind = tree.kind, existing = tree)
		val current = current
		
		val consumptionScope = current.flags.consumptionScope
		if(tree.kind == CstParseContext.NodeKind.LeafNode) {
			debug(
				"will accept node ${tree.debugInfo.name}('${tree.contentToString()}')? yes=${
					consumptionScope.hasParent(parent)
				}",
			)
			if(consumptionScope.hasParent(parent)) {
				if(tree.children.isNotEmpty() && tree.contentToString().isNotEmpty()) {
					throw IllegalStateException("LeafNode should have children xor has span.")
				}
				for(token in tree.tokens) {
					c.acceptToken(token)
				}
			}
		}
		
		for(child in tree.children) {
			acceptNode(child)
		}
		endChildNodeStub(context, tree.node, tree)
	}
	
	override fun hiddenDebugCommands(command: String, vararg args: Any?): Any? {
		when(command) {
			"ensureDetached" -> {
				val group = (args[0] as CstNode).tree as Group
				if(group.debugInfo.attachedTo != null) {
					error("ensureDetached() but attached: kind=${group.kind}, attached=${group.debugInfo.attachedTo}, detached=${group.flags.detached}")
				}
			}
			
			else -> error("unknown command $command")
		}
		
		return null
	}
	
	override fun pushDiagnostic(diagnostic: Diagnostic) {
		println(diagnostic)
	}
}

fun CstNode.dumpTree(context: TextParseContext) {
	(tree as Group).dumpTree(context, 1)
}
