# Llang Compiler Book

> **Note**: this is nothing but a method for me to explain myself how this whole ast structure, lexer, parser and
> compiler works. Nothing educational.

## Tokens and Lexer

Tokens are like 'words' in English. They are flat(not nested/structured).
It says how to split raw code into meaningful components.

I decided to have a very simple handwritten lexer. Lexer is quite powerful that, even
though I'm thinking of a very complex language inspired by Rust and Kotlin, I didn't
squeeze my brain that much. Interface used by lexer
([MutableCodeIterator](modules/tooling/lexer/src/commonMain/kotlin/com/lhwdev/llang/lexer/code/MutableCodeIterator.kt)
and [LexerScope](modules/tooling/lexer/src/commonMain/kotlin/com/lhwdev/llang/lexer/LexerScope.kt))
is just a peek-able iterator with extra things.

Ideally lexer can be defined stateless and large:

``` kotlin
fun analyzeLexically(code: LlangCode): List<Token>

class Token(val value: String, val kind: TokenKind)
```

But we need to implement incremental lexing(IC) later, so we need to change this into
below.

``` kotlin
fun nextToken(code: LlangCode, index: Int): Token
```

We analyze code sequentially from start to end.
Only parameters is `code` and `index`. `index` is initially `0` and increased by
`previousToken.value.length`. Most portion of code works like this. In this way, is
some code is modified, we can start from there, not from start.

But there are some parts where state is needed, like in string literal. See following
code.

``` kotlin
println("Hello, ${user.name}!")
```

`"`, `Hello, `, `${`, `user`, `.`, `name`, `}` is all separate tokens. One may think
returning huge `StringLiteral("Hello, ", /* List<Token> for user.name */)` is easy,
but for IC, we need to be flat. So we have 3 states: root, in string literal, 'in
template expression in string literal'. This partially provides nesting for lexer.
State is saved in stack, and when we pop it, we get parent state.

In actual code, there is `val stringDepth: Int` in state, whose value is like:

```
(depth=0) println("(depth=1) Hello, ${ (depth=2) user.name } (depth=1)!" (depth=0) )
```

If depth is odd, you parse string literal. If depth is even, you parse normal expression.
This rule applies well when you nest template expression a lot.

So lexer is defined like this:

``` kotlin
fun nextToken(code: LlangCode, index: Int, state: State): Pair<Token, StateUpdate>

sealed class StateUpdate {
    object None : StateUpdate()
    class Push(val state: State): StateUpdate()
    object Pop : StateUpdate()
}
```

and actual interfaces are just a wrapper around this.

## Incremental Lexing

In a huge file, lexing whole file again may be burdening, so we incrementally parse
code into tokens. Basic approach here is being conservative, ensuring soundness.

**Steps for Incremental Lexing**
(low level optimizations omitted; shell I use gap buffer?)

1. **Get modification in form of `Patch`**

   `Patch` is just like `String.replace(oldRange, newText)`: find common heading and
   trailing. In IDE, most operation is to insert keystrokes at cursor or delete some
   texts, so it can be easily retrieved.

2. **Find first span affected**
   
   
   
   
   