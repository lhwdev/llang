package com.lhwdev.llang.token


sealed class LlTokenKind(debugName: String) : TokenKind(debugName) {
	open val common: Boolean
		get() = true
}


/**
 * Most tokens are 'meaningful'; which means it should be parsed in regard to its surrounding
 * context. For example, `<` can be arithmetic comparison(Lt; less than), or group start used for
 * generics.
 * But there are some tokens which is meaning-neutral, such as `:` (colon). Although it can be
 * used to mark type of variable, or to mark the parent of class, assigning 'meaning' to it seems
 * useless.
 */
object TokenKinds {
	object Illegal : LlTokenKind("illegal")
	
	sealed class Ws(debugName: String) : LlTokenKind(debugName)
	
	object Whitespace : Ws("whitespace")
	
	class Comment(debugName: String) : Ws(debugName) {
		companion object All : TokenKindSetBuilder("comments") {
			/**
			 * Like `some code // comment`
			 */
			val Eol = +Comment("eol comment")
			
			/**
			 * Like `code /* comment */ other code` which can span several lines
			 */
			val Block = +Comment("block comment")
			
			/**
			 * Like `/** documentation */`. Semantically (mostly) identical to [Block] for
			 * compilation etc., but needed for IDE support.
			 */
			val LDocBlock = +Comment("ldoc block comment")
		}
	}
	
	class LDoc(debugName: String) : Ws("ldoc $debugName") {
		companion object All : TokenKindSetBuilder("ldocs") {
			// TODO
		}
	}
	
	
	sealed class Identifier(debugName: String) : LlTokenKind(debugName) {
		object Simple : Identifier("simpleIdentifier")
		
		object Quoted : Identifier("`quoted identifier`")
	}
	
	sealed class StringLiteral(debugName: String) : LlTokenKind(debugName) {
		class QuoteBegin(debugName: String) : StringLiteral("$debugName begin")
		class QuoteEnd(debugName: String) : StringLiteral("$debugName end")
		class Content(debugName: String) : StringLiteral(debugName)
		
		class Quote(debugName: String) : TokenKindSetBuilder(debugName) {
			val Begin = +QuoteBegin(debugName)
			val End = +QuoteEnd(debugName)
		}
		
		companion object All : TokenKindSetBuilder("string literals") {
			/**
			 * Like `"Hello, world"`
			 */
			val Escaped = +Quote("\"")
			
			/**
			 * Like `"""Raw String; "hi" $var $$var_escape"""` (can span multiple lines)
			 */
			val Raw = +Quote("\"\"\"")
			
			val Literal = +Content("literal")
			
			/**
			 * Like `\r`, `\r`, `\u1a43`
			 */
			val EscapedLiteral = +Content("escaped literal")
			
			/**
			 * `user`
			 */
			val TemplateVariable = +Content("\$variable")
			
			/**
			 * `${3 + 4}`
			 */
			val TemplateExpression = +Content("\${expression}")
		}
	}
	
	class NumberLiteral(debugName: String) : LlTokenKind(debugName) {
		companion object All : TokenKindSetBuilder("number literals") {
			/**
			 * Note: that literal is NumberLiteral.Integer does not mean that literal is going to be
			 * the type of `Int`.
			 */
			val Integer = +NumberLiteral("integer")
			
			val Hex = +NumberLiteral("hex")
			
			val Binary = +NumberLiteral("binary")
			
			val Float = +NumberLiteral("float")
		}
	}
	
	
	sealed class Operation(debugName: String) : LlTokenKind(debugName) {
		class Arithmetic(debugName: String) : Operation(debugName) {
			companion object All : TokenKindSetBuilder("arithmetics") {
				val UnaryPlus = +Arithmetic("+(unary)")
				val UnaryMinus = +Arithmetic("-(unary)")
				
				
				val Plus = +Arithmetic("+")
				val Minus = +Arithmetic("-")
				
				val Times = +Arithmetic("*")
				
				val Divide = +Arithmetic("/")
				
				val Remainder = +Arithmetic("%")
			}
		}
		
		class Compare(debugName: String) : Operation(debugName) {
			companion object All : TokenKindSetBuilder("compares") {
				val Equals = +Compare("==")
				
				val NotEquals = +Compare("!=")
				
				val IdentityEquals = +Compare("===")
				
				val NotIdentityEquals = +Compare("!==")
				
				val Lt = +Compare("<")
				
				val LtEq = +Compare("<=")
				
				val Gt = +Compare(">")
				
				val GtEq = +Compare(">=")
			}
		}
		
		class Logic(debugName: String) : Operation(debugName) {
			companion object All : TokenKindSetBuilder("logics") {
				val And = +Logic("&&")
				
				val Or = +Logic("||")
				
				val Not = +Logic("!")
			}
		}
		
		class Expression(debugName: String) : Operation(debugName) {
			companion object All : TokenKindSetBuilder("expressions") {
				// Range Operator
				
				val RangeTo = +Expression("..")
				
				val RangeUntil = +Expression("..<")
				
				val RangeAfterTo = +Expression("<..")
				
				val RangeAfterUntil = +Expression("<..<")
				
				
				/**
				 * Type instance check operator.
				 * `expression is Type`
				 */
				val Is = +Expression("is")
				
				val NotIs = +Expression("!is")
				
				/**
				 * Collection inclusion operator.
				 * `element in collection` => `Boolean` etc.
				 */
				val In = +Expression("in")
				
				val NotIn = +Expression("!in")
				
				val Elvis = +Expression("?:")
				
				// Infix operator is handled by cst level; not token level.
				// val Infix = +Expression("infix")
			}
		}
		
		class Assign(debugName: String) : Operation(debugName) {
			companion object All : TokenKindSetBuilder("assigns") {
				val Assign = +Assign("=")
				
				val PlusAssign = +Assign("+")
				
				val MinusAssign = +Assign("-")
			}
		}
		
		class Group(debugName: String) : Operation(debugName) {
			companion object All : TokenKindSetBuilder("groups") {
				/**
				 * Usage:
				 * - group expression
				 * - tuple
				 */
				val LeftParen = +Group("(")
				val RightParen = +Group("(")
				
				val LeftSquareBracket = +Group("[")
				val RightSquareBracket = +Group("]")
				
				val LeftBrace = +Group("{")
				val RightBrace = +Group("}")
				
				/**
				 * Only used for type parameters; `<T>`, `<Type : Hello, Hi = 123>`
				 */
				val LeftAngleBracket = +Group("<")
				val RightAngleBracket = +Group(">")
			}
		}
		
		class Access(debugName: String) : Operation(debugName) {
			companion object All : TokenKindSetBuilder("accesses") {
				val Dot = +Access(".")
				
				val SafeDot = +Access("?.")
				
				val Metadata = +Access("::")
			}
		}
		
		class Other(debugName: String) : Operation(debugName) {
			companion object All : TokenKindSetBuilder("others") {
				val Semicolon = +Other(";")
				
				val PropagateError = +Other("?")
				
				val Etc = +Other("...")
				
				
				val Comma = +Other(",")
				
				val Colon = +Other(":")
				
				val ArrowRight = +Other("->")
				
				/**
				 * ```
				 * #[annotation]
				 * declaration
				 * ```
				 */
				val AnnotationMarker = +Other("#")
			}
		}
	}
	
	
	sealed class Special(debugName: String) : LlTokenKind(debugName)
	
	sealed class Keyword(debugName: String) : Special(debugName) {
		class Module(debugName: String) : Keyword(debugName)
		class Declaration(debugName: String) : Keyword(debugName)
		class Literal(debugName: String) : Keyword(debugName)
		class ControlFlow(debugName: String) : Keyword(debugName)
		
		companion object All : TokenKindSetBuilder("keywords") {
			/// Modules
			
			val Module = +Module("module")
			
			val Group = +Module("group")
			
			val Use = +Module("use")
			
			
			/// Declarations
			
			val Class = +Declaration("class")
			
			val Interface = +Declaration("interface")
			
			val Object = +Declaration("object")
			
			val Fun = +Declaration("fun")
			
			val Impl = +Declaration("impl")
			
			val Type = +Declaration("type")
			
			val Val = +Declaration("val")
			
			val Const = +Declaration("const")
			
			val Var = +Declaration("var")
			
			
			/// Boolean Constants
			
			val True = +Literal("true")
			
			val False = +Literal("false")
			
			
			/// Control Flows - conditionals
			
			val If = +ControlFlow("if")
			
			val Else = +ControlFlow("else")
			
			val When = +ControlFlow("when")
			
			
			/// Control Flows - loops
			
			val Loop = +ControlFlow("loop")
			
			val While = +ControlFlow("while")
			
			val For = +ControlFlow("for")
			
			/// Control Flows - escape directions
			
			val Return = +ControlFlow("return")
			
			val Break = +ControlFlow("break")
			
			val Continue = +ControlFlow("continue")
		}
	}
	
	sealed class SoftSpecial(debugName: String) : Special(debugName)
	
	class SoftKeyword(debugName: String) : SoftSpecial(debugName) {
		companion object All : TokenKindSetBuilder("soft keywords") {
			val Constructor = +SoftKeyword("constructor")
			
			val Init = +SoftKeyword("init")
			
			
			val Get = +SoftKeyword("get")
			
			val Set = +SoftKeyword("set")
			
			val Field = +SoftKeyword("field")
			
			
			val Where = +SoftKeyword("where")
			
			val By = +SoftKeyword("by")
			
			/**
			 * `in` of `for(element in collection)`
			 */
			val ForIn = +Operation.Other("in")
		}
	}
	
	sealed class Modifier(debugName: String) : SoftSpecial(debugName) {
		
		class Visibility(debugName: String) : Modifier(debugName)
		class Modality(debugName: String) : Modifier(debugName)
		class General(debugName: String) : Modifier(debugName)
		class Class(debugName: String) : Modifier(debugName)
		class Function(debugName: String) : Modifier(debugName)
		class Member(debugName: String) : Modifier(debugName)
		class TypeParameter(debugName: String) : Modifier(debugName)
		class ValueParameter(debugName: String) : Modifier(debugName)
		
		companion object All : TokenKindSetBuilder("modifiers") {
			/// Visibility
			
			val Public = +Visibility("public")
			
			val Internal = +Visibility("internal")
			
			val Protected = +Visibility("protectted")
			
			val Private = +Visibility("private")
			
			
			/// Modality
			
			val Final = +Modality("final")
			
			val Open = +Modality("open")
			
			val Sealed = +Modality("sealed")
			
			val Abstract = +Class("abstract")
			
			
			/// Etc
			
			val Context = +General("context")
			
			val Expect = +General("expect")
			val Actual = +General("actual")
			
			
			/// ClassKind
			
			val Enum = +Class("enum")
			
			val Value = +Class("value")
			
			val Inner = +Class("inner")
			
			val Companion = +Class("companion")
			
			
			/// Function
			
			val Infix = +Function("infix")
			
			val Suspend = +Function("suspend")
			
			val Inline = +Function("inline")
			
			
			/// Member
			
			val Override = +Member("override")
			
			
			/// Type Parameter
			
			val VarianceIn = +TypeParameter("in")
			
			val VarianceOut = +TypeParameter("out")
			
			val Referential = +TypeParameter("referential")
			
			val Erased = +TypeParameter("erased")
			
			
			// Value Parameter
			
			val Vararg = +ValueParameter("vararg")
			
			val Crossinline = +ValueParameter("crossinline")
		}
	}
}
