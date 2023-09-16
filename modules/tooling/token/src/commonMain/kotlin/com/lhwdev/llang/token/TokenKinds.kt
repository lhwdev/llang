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
	
	object LineBreak : Ws("lineBreak")
	
	
	sealed class Comment(debugName: String) : Ws(debugName) {
		sealed class Kind(debugName: String) : TokenKindSetBuilder(debugName)
		sealed class BlockKind(debugName: String) : Kind(debugName) {
			abstract val Begin: CommentBegin
			abstract val End: CommentEnd
		}
		
		class CommentBegin(debugName: String, val kind: Kind) : Comment(debugName)
		class CommentEnd(debugName: String, val kind: Kind) : Comment(debugName)
		class Content(debugName: String) : Comment(debugName)
		
		
		/**
		 * Like `some code // comment`
		 */
		object Eol : Kind("eolComment") {
			val Begin = +CommentBegin("comment.eol.begin", this)
		}
		
		/**
		 * Like `code /* comment */ other code` which can span several lines
		 */
		object Block : BlockKind("blockComment") {
			override val Begin = +CommentBegin("comment.block.begin", this)
			override val End = +CommentEnd("comment.block.end", this)
		}
		
		/**
		 * Like `/** documentation */`. Semantically (mostly) identical to [Block] for
		 * compilation etc., but needed for IDE support.
		 */
		object LDocBlock : BlockKind("ldocBlockComment") {
			class LDoc(debugName: String) : Ws(debugName)
			
			override val Begin = +CommentBegin("comment.ldocBlock.begin", this)
			override val End = +CommentEnd("comment.ldocBlock.end", this)
			
			// TODO: ldoc elements
		}
		
		companion object All : TokenKindSetBuilder("comments") {
			init {
				+Eol
				+Block
				+LDocBlock
			}
			
			val Content = +Content("comment.content")
		}
	}
	
	
	sealed class Identifier(debugName: String) : LlTokenKind(debugName) {
		object Simple : Identifier("identifier.simple")
		
		object Quoted : Identifier("identifier.quoted")
	}
	
	sealed class StringLiteral(debugName: String) : LlTokenKind(debugName) {
		class QuoteBegin(debugName: String, val quote: Quote) : StringLiteral("${debugName}begin")
		class QuoteEnd(debugName: String, val quote: Quote) : StringLiteral("${debugName}end")
		class Content(debugName: String) : StringLiteral(debugName)
		
		class Quote(debugName: String) : TokenKindSetBuilder(debugName) {
			val Begin = +QuoteBegin(debugName, this)
			val End = +QuoteEnd(debugName, this)
		}
		
		companion object All : TokenKindSetBuilder("stringLiterals") {
			/**
			 * Like `"Hello, world"`
			 */
			val Escaped = +Quote("stringLiteral.escaped")
			
			/**
			 * Like `"""Raw String; "hi" $var $$var_escape"""` (can span multiple lines)
			 */
			val Raw = +Quote("stringLiteral.raw")
			
			val Content = +Content("stringLiteral.content")
			
			/**
			 * Like `\r`, `\r`, `\u1a43`
			 */
			val EscapedContent = +Content("stringLiteral.escapedContent")
			
			/**
			 * `user`
			 */
			val TemplateVariable = +Content("stringLiteral.templateVariable")
			
			/**
			 * `${3 + 4}`
			 */
			val TemplateExpression = +Content("stringLiteral.templateExpression")
		}
	}
	
	class NumberLiteral(debugName: String) : LlTokenKind(debugName) {
		companion object All : TokenKindSetBuilder("numberLiterals") {
			/**
			 * Note: that literal is NumberLiteral.Integer does not mean that literal is going to be
			 * the type of `Int`.
			 */
			val Integer = +NumberLiteral("numberLiteral.integer")
			
			val Hex = +NumberLiteral("numberLiteral.hexInteger")
			
			val Binary = +NumberLiteral("numberLiteral.binaryInteger")
			
			val Float = +NumberLiteral("numberLiteral.float")
		}
	}
	
	
	sealed class Operator(debugName: String) : LlTokenKind(debugName) {
		sealed class OperatorWithPrecedence(debugName: String, val precedence: Int) :
			Operator(debugName)
		
		// precedence = 35x
		class Arithmetic(debugName: String, precedence: Int) :
			OperatorWithPrecedence(debugName, precedence) {
			companion object All : TokenKindSetBuilder("arithmetics") {
				val Plus = +Arithmetic("arithmetic.plus", precedence = 350)
				
				val Minus = +Arithmetic("arithmetic.minus", precedence = 350)
				
				val Times = +Arithmetic("arithmetic.times", precedence = 351)
				
				val Divide = +Arithmetic("arithmetic.divide", precedence = 351)
				
				val Remainder = +Arithmetic("arithmetic.remainder", precedence = 351)
			}
		}
		
		// precedence = 15x
		class Compare(debugName: String, precedence: Int) :
			OperatorWithPrecedence(debugName, precedence) {
			companion object All : TokenKindSetBuilder("compares") {
				val Equals = +Compare("compare.equals", precedence = 150)
				
				val NotEquals = +Compare("compare.notEquals", precedence = 150)
				
				val IdentityEquals = +Compare("compare.identityEquals", precedence = 150)
				
				val NotIdentityEquals = +Compare("compare.notIdentityEquals", precedence = 150)
				
				val Lt = +Compare("compare.lt", precedence = 151)
				
				val LtEq = +Compare("compare.ltEq", precedence = 151)
				
				val Gt = +Compare("compare.gt", precedence = 151)
				
				val GtEq = +Compare("compare.gtEq", precedence = 151)
			}
		}
		
		// precedence = 12x
		class Logic(debugName: String, precedence: Int) :
			OperatorWithPrecedence(debugName, precedence) {
			companion object All : TokenKindSetBuilder("logics") {
				val And = +Logic("logic.and", precedence = 121)
				
				val Or = +Logic("logic.or", precedence = 120)
				
				val Not = +Logic("logic.not", precedence = Int.MAX_VALUE)
			}
		}
		
		// precedence = 40x (U*T-> U'), 20x (U*U -> U), 17x (U*U -> B)
		class Expression(debugName: String, precedence: Int) :
			OperatorWithPrecedence(debugName, precedence) {
			companion object All : TokenKindSetBuilder("expressions") {
				// Range Operator
				
				val RangeTo = +Expression("expression.rangeTo", precedence = 280)
				
				val RangeUntil = +Expression("expression.rangeUntil", precedence = 280)
				
				val RangeAfterTo = +Expression("expression.rangeAfterTo", precedence = 280)
				
				val RangeAfterUntil = +Expression("expression.rangeAfterUntil", precedence = 280)
				
				
				/**
				 * Type instance check operator.
				 * `expression is Type`
				 */
				val Is = +Expression("expression.is", precedence = 170)
				
				val NotIs = +Expression("expression.notIs", precedence = 170)
				
				/**
				 * Type cast operator.
				 */
				val As = +Expression("expression.as", precedence = 400)
				
				val AsOrNull = +Expression("expression.asOrNull", precedence = 400)
				
				/**
				 * Collection inclusion operator.
				 * `element in collection` => `Boolean` etc.
				 */
				val In = +Expression("expression.in", precedence = 170)
				
				val NotIn = +Expression("expression.notIn", precedence = 170)
				
				val Elvis = +Expression("expression.elvis", precedence = 120)
				
				val Infix = +Expression("expression.infix", precedence = 270)
			}
		}
		
		class Assign(debugName: String) : Operator(debugName) {
			companion object All : TokenKindSetBuilder("assigns") {
				val Assign = +Assign("assign.assign")
				
				val PlusAssign = +Assign("assign.plusAssign")
				
				val MinusAssign = +Assign("assign.minusAssign")
			}
		}
		
		class Group(debugName: String, val open: Boolean) : Operator(debugName) {
			companion object All : TokenKindSetBuilder("groups") {
				/**
				 * Usage:
				 * - group expression
				 * - tuple
				 */
				val LeftParen = +Group("group.leftParen", open = true)
				
				val RightParen = +Group("group.rightParen", open = false)
				
				val LeftSquareBracket = +Group("group.leftSquareBracket", open = true)
				
				val RightSquareBracket = +Group("group.rightSquareBracket", open = false)
				
				val LeftBrace = +Group("group.leftBrace", open = true)
				
				val RightBrace = +Group("group.rightBrace", open = false)
				
				/**
				 * Only used for type parameters; `<T>`, `<Type : Hello, Hi = 123>`
				 */
				val LeftAngleBracket = +Group("group.leftAngleBracket", open = true)
				
				val RightAngleBracket = +Group("group.rightAngleBracket", open = false)
			}
		}
		
		class Access(debugName: String) : Operator(debugName) {
			companion object All : TokenKindSetBuilder("accesses") {
				val Dot = +Access("access.dot")
				
				val Metadata = +Access("access.metadata")
			}
		}
		
		/**
		 * Things that is meaningless by token itself. For example, 'number literal' has some
		 * intrinsic meaning, but 'colon' is meaning-agnostic if without context that 'it is used
		 * for type declaration.'
		 *
		 * ...Or things that cannot belong to other category.
		 */
		class Other(debugName: String) : Operator(debugName) {
			companion object All : TokenKindSetBuilder("others") {
				val Semicolon = +Other("other.semicolon")
				
				val PropagateError = +Other("other.propagateError")
				
				val Etc = +Other("other.etc")
				
				
				val Comma = +Other("other.comma")
				
				val Colon = +Other("other.colon")
				
				val ArrowRight = +Other("other.arrowRight")
				
				/**
				 * ```
				 * #[annotation]
				 * declaration
				 * ```
				 */
				val AnnotationMarker = +Other("other.annotationMarker")
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
			
			val Module = +Module("keyword.module")
			
			val Group = +Module("keyword.group")
			
			val Use = +Module("keyword.use")
			
			
			/// Declarations
			
			val Class = +Declaration("keyword.class")
			
			val Interface = +Declaration("keyword.interface")
			
			val Object = +Declaration("keyword.object")
			
			val Fun = +Declaration("keyword.fun")
			
			val Impl = +Declaration("keyword.impl")
			
			val Type = +Declaration("keyword.type")
			
			val Val = +Declaration("keyword.val")
			
			val Const = +Declaration("keyword.const")
			
			val Var = +Declaration("keyword.var")
			
			
			/// Boolean Constants
			
			val True = +Literal("keyword.true")
			
			val False = +Literal("keyword.false")
			
			
			/// Control Flows - conditionals
			
			val If = +ControlFlow("keyword.if")
			
			val Else = +ControlFlow("keyword.else")
			
			val When = +ControlFlow("keyword.when")
			
			
			/// Control Flows - loops
			
			val Loop = +ControlFlow("keyword.loop")
			
			val While = +ControlFlow("keyword.while")
			
			val For = +ControlFlow("keyword.for")
			
			/// Control Flows - escape directions
			
			val Return = +ControlFlow("keyword.return")
			
			val Break = +ControlFlow("keyword.break")
			
			val Continue = +ControlFlow("keyword.continue")
		}
	}
	
	sealed class SoftSpecial(debugName: String) : Special(debugName)
	
	class SoftKeyword(debugName: String) : SoftSpecial(debugName) {
		companion object All : TokenKindSetBuilder("softKeywords") {
			val Constructor = +SoftKeyword("softKeyword.constructor")
			
			val Init = +SoftKeyword("softKeyword.init")
			
			
			val Get = +SoftKeyword("softKeyword.get")
			
			val Set = +SoftKeyword("softKeyword.set")
			
			val Field = +SoftKeyword("softKeyword.field")
			
			
			val Where = +SoftKeyword("softKeyword.where")
			
			val By = +SoftKeyword("softKeyword.by")
			
			/**
			 * `in` of `for(element in collection)`
			 */
			val ForIn = +Operator.Other("softKeyword.in")
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
			
			val Protected = +Visibility("protected")
			
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
