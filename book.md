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
fun nextToken(code: LlangCode): Pair<Token, LlangCode> // currentToken to remainingCode
```

We analyze code sequentially from start to end, and this function can be reduced into
`List<Token>`.
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

In pure functional programming, you can just pass stack of state along with `LlangCode`.
But... I decided to write it like a state machine. Rewriting it in FP would be fun! (TODO?)

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

1. **Get modification in form of `CodeModification`**

   `CodeModification` is just like `String.replace(oldRange, newText)`: find common
   heading and trailing. In IDE, most operation is to insert keystrokes at cursor or
   delete some texts, so it can be easily retrieved.

   TODO: accept Patch which has multiple CodeModification (which does not overlap).
   Shell we Myers-diff?

2. **Find first span affected**

   Choose which tokens to start parsing at. If you choose wrong token, you may encounter
   soundness problem. In simplest way, choose the very token containing `oldRange.first`.
   But this may cause problems. Think `Patch` from `Hello World` to `HelloWorld`. Two
   distinct tokens are joined into one, but if start lexing from after 'Hello', you will
   get two tokens for `HelloWorld`, which is definitely wrong. To mitigate this, I
   introduced 'separator token'. Separator tokens are always interpreted as same
   `TokenKind`, like `(`, `)`, `,`. `>` cannot be separator as it can be interpreted as
   `>` (Gt), `>=` (GtEq), or `->` (RightArrow). Think of when `- >` becomes `->` vs.
   `hello()` becomes `hello( )` or anything.

3. **Begin parsing with state**

   You can just start parsing like initial, but it isn't that simple. For example, if we
   are in `" "` or `/* */`, need to parse differently.How can we get current state?
   Well, it is as simple as traversing `PushState` or `PopState` ahead and find matching
   closest `PushState` with same level. This also can be cached in common situations
   like inserting code in IDE, as you are in same context across inputs.

   TODO: cache like what I said above

4. **How far shell we go?**

   After we go past boundary of `CodeModification`, (`offset >= newSpan.end`) if lexer
   yield the same token in the same location and state, we can conclude that following
   tokens will be identical as lexing goes from start to end. 🚧 TODO: WIP

## Cst Parser

### Problem 1: range of code

Llang does not require semicolon `;` at the end of a statement. Parser should figure it
out. How can we?

It comes out that it is not that hard. This comprises 3 simple steps.

1. If `depth > 0`, continue to next line.
   If code has unmatched group open like `(` and `{`, continue parsing.
   Note that for maximum resistance for IDE, we should cut out and leave statement
   incomplete for some cases, like below:
   ```
   val a = hello("lhwdev", // work in progress here
   val b = 3
   ```
   To find matching brace `)` will require invalidation of whole code, maybe causing lag.

2. If next line can be parsed independently, make it independent.
   For example, suppose a code like this:
   ``` kotlin
   val number = 3
   + 2
   ```

   Here, one may think number should be 5, as it becomes `(3 + 2)`, but it isn't. We will
   cut following line if it can be independent in any cases. If there is a code like
   `val abc = true (next line) && false`, `&& false` cannot be a separate statement, and
   they are joined. So this requires some reduce-like workflow for parsing statements.

3. Join the lines otherwise, like example in 2.

### Problem 2: Operator precedence

~~WTF which algorithm I should use? Should I traverse all operators from high precedence
to low one grouping all statements?~~

#### Proposed method 1. local maximum

> Note: This must be a known method, but I am finding out myself.
> Headache...

Think of following case where only binary operators exist.

We will find operations based on local maximum point.
Think of graph which is connected lines of dots of
`f(operator_index) = operator_precedence`. Operator with maximum precedence will be shown
as maximum. Interestingly, not only maximum points but also all **local** maximum points
can be grouped into operations first. As such operations become element into other
operation, it no longer has operator to consider; so we remove its operation from list.

What happens next? If one operation is removed, operator right in front of removed one
may become local maximum. Think of operator precedences list `[1, 3, 4, 2]` then `4` is
removed. In `[1, 3, 2]`, `3`(which is right in front of `4`) becomes local maximum.
Of course this is not always the case.

```kotlin
a = 3 + 4 * 7 + 1
```

Operator precedences for this is `=`(1) < `+`(2) < `*`(3).
Parsing consists of iterating over operators.

(code body parser algorithm v1)

| stack               | buffer(stack)             | state for lookahead    | operation to run |
|---------------------|---------------------------|------------------------|------------------|
|                     | a = 3 + 4 * 7 + 1         | initial                | push             |
| a =                 | 3 + 4 * 7 + 1             | ascending(1 -> 2)      | push             |
| a = 3 +             | 4 * 7 + 1                 | ascending(2 -> 3)      | push             |
| a = 3 + 4 *         | 7 + 1                     | **descending**(3 -> 2) | **ops**          |
| a = 3 +             | (4 * 7) + 1               | **equal**(1 -> 1)      | **ops**          |
| a =                 | (3 + (4 * 7)) + 1         | ascending(1 -> 2)      | push             |
| a = (3 + (4 * 7)) + | 1                         | **eof**                | **ops**          |
| a =                 | ((3 + (4 * 7)) + 1)       | **eof**                | **ops**          |
|                     | (a = ((3 + (4 * 7)) + 1)) | **eof**                | (done)           |

There are two operation: push and ops.
In `push`, pop two tokens(element + operator) from buffer and add it to stack.
In `ops`, pop two tokens(element + operator) from stack, combine with
`buffer.pop()`(element) and make them a binary operation, then push into buffer.

-----

But, in this method, available syntax is too restricted. We need more flexibility, like
unary operator or group.

To define how much flexibility we need, we should define operations.

(Note: `precedence = eager` means highest one)

| operator                  | name                            | kind          | precedence | example                                |
|---------------------------|---------------------------------|---------------|------------|----------------------------------------|
| `()`                      | expression.group                | unary, group  | eager      | `4 * (1 + 3)`                          |
| `v(p)`                    | expression.call                 | binary, group | eager      | `println("hello, world!")`             |
| `.`                       | memberAccess                    | binary        | eager      | `value.member`, `Class.Other`          |
| `?.`                      | expression.safeMemberAccess     | binary        | eager      | `value?.member`                        |
| `+`/`-`                   | arithmetic.unaryPlus/unaryMinus | unary.prefix  |            | `-7`, `+3`                             |
| `!`                       | logic.not                       | unary.prefix  |            | `!isHello`                             |
| `as`                      | typeOps.cast                    | binary        |            | `parent as Child`                      |
| `as?`                     | typeOps.safeCast                | binary        |            | `parent as? Child`                     |
| `*`/`/`                   | arithmetic.multiply/divide      | binary        |            | `3 * 5`                                |
| `+`/`-`                   | arithmetic.plus/minus           | binary        |            | `3 + 2`                                |
| `..` etc                  | expression.rangeTo ...          | binary        |            | `1..10`                                |
| _identifier_              | expression.infixCall            | binary        |            | `0x10 xor 0x11`                        |
| `?:`                      | expression.elvis                | binary        |            | `optional ?: default`                  |
| `in`/`!in`                | expression.in/notIn             | binary        |            | `"lhwdev" in users`                    |
| `is`/`!is`                | typeOps.is/notIs                | binary        |            | `animal is Dog`                        |
| `<`/`>`/`<=`/`>=`         | logic.lt/gt/ltEq/gtEq           | binary        |            | `age >= 19`                            |
| `==`/`!=`/`===`/`!==`     | logic.equals/identityEquals ... | binary        |            | `you == me`                            |
| `&&`                      | logic.conjunction               | binary        |            | `you.age >= 19 && you.height >= 180`   |
| <code>&#124;&#124;</code> | logic.disjunction               | binary        |            | <code>idiot &#124;&#124; genius</code> |
| `...`                     | functionSpreadArguments         | unary.prefix  |            | `println(...list)`                     |
| `=`/`+=` etc.             | assignment ...                  | binary        |            | `myVar = 3`                            |

You can see that all '(mostly) any-place' operators can be divided into unary/binary.
