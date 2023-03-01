package com.lhwdev.llang.token


sealed class LlTokenKind(debugName: String, group: TokenGroup) : TokenKind(debugName, group) {
	open val common: Boolean
		get() = true
}


object TokenKinds {
	class Illegal(val reason: String? = null) : LlTokenKind("illegal", TokenGroup.Other) {
		override val common: Boolean
			get() = false
		
		override fun toString(): String = "Illegal token: $reason"
	}
	
	object Eof : LlTokenKind("eof", TokenGroup.Separator)
	
	/**
	 * All adjacent whitespaces should be merged into one.
	 */
	object WhiteSpace : LlTokenKind("whitespace", TokenGroup.Separator)
	
	/**
	 * Standard `\n`, `\r`, or `\r\n`.
	 */
	object Eol : LlTokenKind("eol", TokenGroup.Separator)
	
	object Identifier : LlTokenKind("identifier", TokenGroup.Word)
	
	sealed class StringLiteral(debugName: String, group: TokenGroup) : LlTokenKind(debugName, group) {
		class QuoteBegin(debugName: String) : StringLiteral("$debugName(begin)", TokenGroup.WordOpen)
		class QuoteEnd(debugName: String) : StringLiteral("$debugName(end)", TokenGroup.WordClose)
		class Quote(debugName: String) : TokenKindSetBuilder(debugName) {
			val Begin = +QuoteBegin(debugName)
			val End = +QuoteEnd(debugName)
		}
		
		class Content(debugName: String) : StringLiteral(debugName, TokenGroup.Other)
		
		companion object All : TokenKindSetBuilder("string literals") {
			/**
			 * Like `"Hello, world!" "escape \" string"`
			 */
			val Escaped = +Quote("\"")
			
			/**
			 * Like `"""Raw string "hi" $variable $$variable_escape"""`
			 */
			val Raw = +Quote("\"\"\"")
			
			
			val Literal = +Content("literal")
			
			/**
			 * Like `\r` `\n` `\$` `\u1234`
			 */
			val EscapedLiteral = +Content("escaped literal")
			
			/**
			 * $variable
			 */
			val TemplateVariable = +Content("\$variable")
			
			/**
			 * ${expression}
			 */
			val TemplateExpression = +Content("\${expression}")
		}
	}
	
	class NumberLiteral(debugName: String) : LlTokenKind("$debugName literal", TokenGroup.Word) {
		companion object All : TokenKindSetBuilder("number literals") {
			/**
			 * Integer literal, which can be `IntN`, (Int8, Int16, Int32, ..., Byte) `FloatN`, `UIntN`, `UFloatN`, etc.
			 */
			val Integer = +NumberLiteral("integer")
			
			val Hex = +NumberLiteral("hex")
			
			val Binary = +NumberLiteral("binary")
			
			/**
			 * Float literal, which can be `FloatN`, (Float32, Float64, ...), `UFloatN`, etc.
			 */
			val Float = +NumberLiteral("float")
		}
	}
	
	class Comment(debugName: String) : LlTokenKind(debugName, TokenGroup.Separator) {
		companion object All : TokenKindSetBuilder("comments") {
			/**
			 * Like `some code // comment`
			 */
			val Eol = +Comment("// eol comment")
			
			/**
			 * Like `code /* comment */ other code` which can span multiline
			 */
			val Block = +Comment("/* block comment */")
			
			/**
			 * Like `/** document for declaration */ declaration`
			 */
			val LDocBlock = +Comment("/** LDoc */")
			
			/**
			 * Like `#!/usr/bin/bash`. Only allowed at the first line of LlangFile.
			 * As this is ignored by parser, this is categorized as comment.
			 */
			val Shebang = +Comment("#!shebang")
		}
	}
	
	
	sealed class Operation(debugName: String, group: TokenGroup = TokenGroup.Operator) : LlTokenKind(debugName, group) {
		class Arithmetic(debugName: String) : Operation(debugName)
		class Compare(debugName: String) : Operation(debugName)
		class Logic(debugName: String) : Operation(debugName)
		class Expression(debugName: String) : Operation(debugName)
		class Assign(debugName: String) : Operation(debugName)
		class Group(debugName: String) : Operation(debugName, group = TokenGroup.Separator)
		class Access(debugName: String) : Operation(debugName)
		class Other(debugName: String, group: TokenGroup = TokenGroup.Operator) : Operation(debugName, group)
		
		companion object All : TokenKindSetBuilder("operations") {
			/// Arithmetic
			
			val Plus = +Arithmetic("+") // unary(prefix) or binary
			
			val Minus = +Arithmetic("-") // unary(prefix) or binary
			
			val Times = +Arithmetic("*")
			
			val Divide = +Arithmetic("/")
			
			// val IntDivide = +Arithmetic("//")
			
			val Remainder = +Arithmetic("%")
			
			
			// Compare
			
			val Equals = +Compare("==")
			
			val NotEquals = +Compare("!=")
			
			val IdentityEquals = +Compare("===")
			
			val NotIdentityEquals = +Compare("!==")
			
			/**
			 * Used for:
			 * - comparison
			 * - type parameter/argument
			 */
			val Lt = +Compare("<")
			
			val LtEq = +Compare("<=")
			
			val Gt = +Compare(">")
			
			val GtEq = +Compare(">=")
			
			
			/// Logic
			
			val And = +Logic("&&")
			
			val Or = +Logic("||")
			
			val Not = +Logic("!")
			
			
			/// Other expressions
			
			val RangeTo = +Expression("..")
			
			val RangeUntil = +Expression("..<")
			
			val Is = +Expression("is")
			
			val NotIs = +Expression("!is")
			
			/**
			 * - boolean expression (-> contains)
			 * - for loop `for(element in collection)`
			 * - type projection: `<in Type>`
			 */
			val In = +Expression("in")
			
			val NotIn = +Expression("!in")
			
			
			/// Assign
			
			/**
			 * Used for:
			 * - variable initialization
			 * - set variable value
			 * - default value of value parameter
			 * - named value argument
			 */
			val Eq = +Assign("=")
			
			val PlusEq = +Assign("+=")
			
			val MinusEq = +Assign("-=")
			
			
			/// Group
			
			/**
			 * Used for:
			 * - group expressions
			 * - function invocation
			 * - tuple literal (must contain comma)
			 * - destructuring `val (x, y) = point`
			 */
			val LeftParen = +Group("(")
			
			val RightParen = +Group(")")
			
			val LeftBracket = +Group("[")
			
			val RightBracket = +Group("]")
			
			/**
			 * Used for:
			 * - lambda expression
			 */
			val LeftBrace = +Group("{")
			
			val RightBrace = +Group("}")
			
			// Instead of +Group("<") +Group(">") we use Lt / Gt
			// Also <, > is not group; they are not separator
			
			
			/// Access
			
			val Dot = +Access(".")
			
			val SafeCall = +Access("?.")
			
			val Elvis = +Access("?:")
			
			
			/// Other
			
			val PropagateError = +Access("?")
			
			/**
			 * Used for:
			 * - function parameters
			 * - tuple literal
			 */
			val Comma = +Other(",", group = TokenGroup.Separator)
			
			/**
			 * Used for:
			 * - mark type of variable
			 * - mark parent classes or interfaces
			 */
			val Colon = +Other(":", group = TokenGroup.Separator)
			
			/**
			 * Used to:
			 * - divide lambda parameters and body
			 * - mark condition / expression in `when`
			 */
			val ArrowRight = +Other("->")
			
			/**
			 * ```
			 * #[annotation]
			 * declaration
			 * ```
			 */
			val Annotation = +Other("#")
		}
	}
	
	
	// hard
	sealed class Keyword(debugName: String) : LlTokenKind(debugName, TokenGroup.Word) {
		class Module(debugName: String) : Keyword(debugName)
		class Declaration(debugName: String) : Keyword(debugName)
		class Literal(debugName: String) : Keyword(debugName)
		class ControlFlow(debugName: String) : Keyword(debugName)
		
		companion object All : TokenKindSetBuilder("keywords") {
			/// Module
			
			val Module = +Module("module")
			
			val Group = +Module("group")
			
			val Use = +Module("use")
			
			
			/// Declarations
			
			val Class = +Declaration("class")
			
			val Interface = +Declaration("interface")
			
			val Object = +Declaration("object")
			
			val Fun = +Declaration("function")
			
			/**
			 * Kinda 'extension implement' thing.
			 * Importing this is by the name of target class, rather than implementing interface,
			 * (`Class` in `impl Interface for Class`) all impls to `Class` within same file of `Class` will be
			 * exported without any extra imports, meaning this is similar to `: SuperClass`, without that it cannot
			 * access private properties.
			 */
			val Impl = +Declaration("impl")
			
			/**
			 * Note that all constant, value, variable is generalized into 'variable'.
			 *
			 * ## Considerations around variable keyword
			 * (**immutable** / read-only / mutable)
			 * - `var` / _ / `mut var`: variables might not be variable(mutable)
			 * - _ / `val` / `var`: I do not think this is confusing, but I want immutable to be a feature of this lang
			 * - `const` / `val` / `var`: I think this is good?
			 *
			 * ## Final Decisions
			 * `const` for constants(immutable), `val` for read-only value, `var` for variable(mutable).
			 */
			val Const = +Declaration("constant")
			
			val Val = +Declaration("value")
			
			val Var = +Declaration("variable")
			
			
			/// Literals
			
			val True = +Literal("true")
			
			val False = +Literal("false")
			
			// val Null = +Literal("null")
			
			
			/// Expressions
			
			/// Control flows - conditionals
			
			val If = +ControlFlow("if")
			
			val Else = +ControlFlow("else")
			
			val When = +ControlFlow("when")
			
			/// Control flows - loops
			
			val Loop = +ControlFlow("loop")
			
			val While = +ControlFlow("while")
			
			val For = +ControlFlow("for")
			
			val Do = +ControlFlow("do") // used in do-while loop
			
			/// Control flows - directions
			
			val Return = +ControlFlow("return")
			
			val Break = +ControlFlow("break")
			
			val Continue = +ControlFlow("continue")
		}
	}
	
	
	// soft
	class SoftKeyword(debugName: String) : LlTokenKind(debugName, TokenGroup.Word) {
		companion object All : TokenKindSetBuilder("soft keywords") {
			val Constructor = +SoftKeyword("constructor")
			
			val Init = +SoftKeyword("init")
			
			
			val Get = +SoftKeyword("get")
			
			val Set = +SoftKeyword("set")
			
			val Field = +SoftKeyword("field")
			
			
			/**
			 * ```
			 * class MyClass<T, R> where T : R
			 * ```
			 */
			val Where = +SoftKeyword("where")
			
			val By = +SoftKeyword("by")
			
		}
	}
	
	
	// soft
	sealed class Modifier(debugName: String) : LlTokenKind(debugName, TokenGroup.Word) {
		class Visibility(debugName: String) : Modifier(debugName)
		class Modality(debugName: String) : Modifier(debugName)
		class General(debugName: String) : Modifier(debugName)
		class Class(debugName: String) : Modifier(debugName)
		class Function(debugName: String) : Modifier(debugName)
		class Member(debugName: String) : Modifier(debugName)
		class TypeParameter(debugName: String) : Modifier(debugName)
		class ValueParameter(debugName: String) : Modifier(debugName)
		
		companion object All : TokenKindSetBuilder("modifiers") {
			/// Visibility (general)
			
			val Public = +Visibility("public")
			
			val Internal = +Visibility("internal")
			
			val Protected = +Visibility("protected")
			
			val Private = +Visibility("private")
			
			
			/// Modality (general)
			
			/**
			 * All non-abstract classes and members (functions and variables, including overriding ones) are final.
			 */
			val Final = +Modality("final")
			
			val Open = +Modality("open")
			
			val abstract = +General("abstract")
			
			val Sealed = +Class("sealed")
			
			/// Etc (general)
			
			val Context = +General("context")
			
			val Expect = +General("expect")
			val Actual = +General("actual")
			
			
			/// ClassKind
			
			val Enum = +Class("enum")
			
			/**
			 * Class with `value` modifier is stored and passed as value. If you need a reference to value class,
			 * use `Ref<T>`. If you need to possess a value class as reference, use `Box<T>`.
			 */
			val Value = +Class("value")
			
			val Inner = +Class("inner")
			
			val Companion = +Class("companion")
			
			
			/// Function
			
			val AnnotationDeclaration = +Class("annotation")
			
			val Infix = +Function("infix")
			
			val Suspend = +Function("suspend")
			
			val Tailrec = +Function("tailrec")
			
			/**
			 * Llang automatically inlines functions if necessary for performance. But if inlining a function changes
			 * semantic of calling that function, use `inline` keyword. Note that, If you are to enforce inline
			 * optimization, use `#[inline]` annotation instead.
			 */
			val Inline = +Function("inline")
			
			
			/// Member
			
			val Override = +Member("override")
			
			
			/// Type Parameter
			
			// val In = +TypeParameter("in") // see Operation.In; `in` is hard keyword(operation)
			
			val Out = +TypeParameter("out")
			
			val StarProjection = +TypeParameter("star")
			
			val Erased = +TypeParameter("erased")
			
			
			/// Value Parameter
			
			val Vararg = +ValueParameter("vararg")
			
			/**
			 * Enforces a semantic that inside a lambda value parameter for a inline function, you should not
			 * early return.
			 */
			val CrossInline = +ValueParameter("crossinline")
			
			// noinline keyword is for optimization rather than for semantics.
			// Compiler takes care of it, and if you want to force it, use [noinline] annotation.
			// val NoInline = +ValueParameter("noinline")
		}
	}
}
