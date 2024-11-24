# Llang Compiler Book

> **Note**: this is nothing but a method for me to explain myself how this whole ast structure,
> tokenizer, parser and compiler works. Nothing educational.

## Cst vs Ast vs Fir

**Cst**: lexical representation of code, mostly as-is. Note that tokens that may be used to
determine the structure but that are no longer needed only exists in CstRTree (cst raw tree). Can
easily represent error tree.

**Ast**: more abstract, lexical representation of code. Some type-safety (inside tooling code) is
achieved here. Note that, in Cst we used opaque types of `Token`, `CstModifiers`, etc. which were
flexible enough to represent errors. Ast cannot represent errors by default, unless supported.

**Fir**: semantically fully-resolved tree. Contains 'invisible' elements such as inferred type or
colors. Fir cannot represent error tree.

## Tokens and Tokenizer

> Note that tokenizer phase is integrated into Cst parsing phase.
> All tokenization phase described here are obsolete.

Tokens are like 'words' in English. They are flat(not nested/structured).
It says how to split raw code into meaningful components.

I decided to have a very simple handwritten tokenizer. Tokenizer is quite powerful that, even
though I'm thinking of a very complex language inspired by Rust and Kotlin, I didn't
squeeze my brain that much. Interface used by tokenizer
([MutableCodeIterator](modules/tooling/tokenizer/src/commonMain/kotlin/com/lhwdev/llang/tokenizer/code/MutableCodeIterator.kt)
and [TokenizerScope](modules/tooling/tokenizer/src/commonMain/kotlin/com/lhwdev/llang/tokenizer/TokenizerScope.kt))
is just a peek-able iterator with extra things.

Ideally tokenizer can be defined stateless and large:

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
template expression in string literal'. This partially provides nesting for tokenizer.
State is saved in stack, and when we pop it, we get parent state.

In actual code, there is `val stringDepth: Int` in state, whose value is like:

```
(depth=0) println("(depth=1) Hello, ${ (depth=2) user.name } (depth=1)!" (depth=0) )
```

If depth is odd, you parse string literal. If depth is even, you parse normal expression.
This rule applies well when you nest template expression a lot.

In pure functional programming, you can just pass stack of state along with `LlangCode`.
But... I decided to write it like a state machine. Rewriting it in FP would be fun! (TODO?)

So tokenizer is defined like this:

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

   After we go past boundary of `CodeModification`, (`offset >= newSpan.end`) if tokenizer
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

> Note: I figured out whole things myself, but some known method should exist.
> Headache...

> Note 2: If this method is wrong, please create an issue. ~~If this method is right, praise me.~~

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
a = 3 + 4 * 7 xor 1
```

Operator precedences for this is `=`(1) < `xor`(2) < `+`(3) < `*`(4).
Parsing consists of iterating over operators.

**Local Maximum Precedence Parsing Algorithm v1**

| stack                               | buffer queue          | state for lookahead    | operation to run |
|-------------------------------------|-----------------------|------------------------|------------------|
| (stack/head)                        | `a = 3 + 4 * 7 xor 1` | initial                | initialPush      |
| ` ` / `a`                           | `= 3 + 4 * 7 xor 1`   | initial                | push             |
| `a =` / `3`                         | `+ 4 * 7 xor 1`       | ascending(1 -> 3)      | push             |
| `a = 3 +` / `4`                     | `* 7 xor 1 `          | ascending(3 -> 4)      | push             |
| `a = 3 + 4 *` / `7`                 | `xor 1`               | **descending**(4 -> 2) | **ops**          |
| `a = 3 +` / `(4 * 7)`               | `xor 1`               | **descending**(3 -> 2) | **ops**          |
| `a =` / `(3 + (4 * 7))`             | `xor 1`               | ascending(1 -> 2)      | push             |
| `a = (3 + (4 * 7)) xor` / `1`       | ` `                   | **eof**                | **ops**          |
| `a =` / `((3 + (4 * 7)) xor 1) `    | ` `                   | **eof**                | **ops**          |
| ` ` / `(a = ((3 + (4 * 7)) xor 1))` | ` `                   | **eof**                | (done)           |

There are three operation: initialPush, push and ops.

In _push_, does following tasks sequentially:

- Push `head` into `stack`.
- Pop `buffer`, then push it into `stack`.
- Pop `buffer`, then assign it into `head`.

In _ops_:

- Pop two tokens(lhs + operator) from `stack`.
- Combine with `head`(rhs) to become a binary operation.
- Assign it into `head`.

In _initialPush_:

- Pop `buffer`, then assign it into `head`.

Logic to determine which to call:

- If `head` is empty, call _initialPush_.
- If `buffer` is empty,
  - If, stack is empty, we're done. Get your `head`.
  - Otherwise, call _ops_.
- If `buffer.peek().precedence <= stack.peek().precedence`, (that is, `state for lookahead` is
  'descending' or 'equal') call _ops_.
- Otherwise, call _push_.

-----

But, in this method, available syntax is too restricted. We need more flexibility, like
unary operator or group.

To define how much flexibility we need, we should define operations.

(Note: `precedence = eager` means highest one)

**Operations from highest to lowest precedence**

| operator                  | name                            | kind          | precedence | example                                |
|---------------------------|---------------------------------|---------------|------------|----------------------------------------|
| `()`                      | expression.group                | unary, group  | eager      | `4 * (1 + 3)`                          |
| `(a, b, ...)`             | expression.tuple                | unary, group  | special    | `(1, 2, 3)`                            |
| `v(a, b, ...)`            | expression.call                 | binary, group | eager      | `println("hello, world!")`             |
| `v[a, b, ...]`            | expression.getElement           | binary, group | eager      | `println("hello, world!")`             |
| `.`                       | memberAccess                    | binary        | eager      | `value.member`, `Class.Other`          |
| `::`                      | metadataAccess                  | binary        | eager      | `Class::Other`                         |
| `+`/`-`                   | arithmetic.unaryPlus/unaryMinus | unary.prefix  | eager'     | `-7`, `+3`                             |
| `!`                       | logic.not                       | unary.prefix  | eager'     | `!isHello`                             |
| `?`                       | propagateError                  | unary.suffix  | eager''    | `println(...list)`                     |
| `as`                      | typeOps.cast                    | binary        | 400        | `parent as Child`                      |
| `as?`                     | typeOps.safeCast                | binary        | 400        | `parent as? Child`                     |
| `*`/`/`/`%`               | arithmetic.multiply/divide/rem  | binary        | 351        | `3 * 5`                                |
| `+`/`-`                   | arithmetic.plus/minus           | binary        | 350        | `3 + 2`                                |
| `..` etc                  | expression.rangeTo ...          | binary        | 280        | `1..10`                                |
| _identifier_              | expression.infixCall            | binary        | 270        | `0x10 xor 0x11`                        |
| `?:`                      | expression.elvis                | binary        | 210        | `optional ?: default`                  |
| `in`/`!in`                | expression.in/notIn             | binary        | 170        | `"lhwdev" in users`                    |
| `is`/`!is`                | typeOps.is/notIs                | binary        | 170        | `animal is Dog`                        |
| `<`/`>`/`<=`/`>=`         | logic.lt/gt/ltEq/gtEq           | binary        | 151        | `age >= 19`                            |
| `==`/`!=`/`===`/`!==`     | logic.equals/identityEquals ... | binary        | 150        | `you == me`                            |
| `&&`                      | logic.conjunction               | binary        | 121        | `you.age >= 19 && you.height >= 180`   |
| <code>&#124;&#124;</code> | logic.disjunction               | binary        | 120        | <code>idiot &#124;&#124; genius</code> |
| `...`                     | functionSpreadArguments         | unary.prefix  | lowest     | `println(...list)`                     |
| `=`/`+=` etc.             | assignment ...                  | binary        | lowest     | `myVar = 3`                            |

Luckily, except for all eager operations, all unary operations has highest/lowest precedence,
which means we can use the 'local maximum approach' almost as-is.

----

We need the logic above the table to include unary operations and groups. When we handle groups,
think that we handle new fresh code starting after `(`. If we meet `)`, it becomes eof.

Therefore, the final logic is:

**In _initialPush_**:

- Pop `buffer`, then Put it into `head`.

**In _push_**, run following tasks sequentially:

- Push `head` into `stack`.
- Pop `buffer`, then push it into `stack`.
- Pop `buffer`, then Put it into `head`.

**In _binaryOps_**:

- Pop two tokens(lhs + operator) from `stack`.
- Combine with `head`(rhs) to become a binary operation.
- Put it into `head`.

**In _unaryOps_**:

- Combine `head` and `buffer.pop()` into unary operation.
- Put it into `head`.

**In _accessOps_**:

- Combine `head`(lhs), `buffer.pop()`(dot etc.), `buffer.pop()`(rhs) into access operation, to
  become `lhs.rhs`, `lhs?.rhs` or `lhs::rhs`.
- Put it into `head`.

**In _callOps_**:

- Push `head`(function) into `stack`.
- Put `buffer.pop()`(group start) into `head`.
- Run _groupOrTupleOps_ with 'tuple mode'. (Saying 'this was tuple!' in advance)
- Combine `stack.pop()`(function) and `head`(tuple) into function invocation.
- Assign it into `head`.

**In _groupOrTupleOps_**:

- Run new fresh parsing with `buffer`. (Note that `buffer` does not contain group start)
- If the result says 'this was tuple!', should I write how to parse tuple?
- Combine `head`(group start), result of 'new fresh parsing', `buffer.pop()`(group end) into a
  group. Note that 'new fresh parsing' already consumed buffer as much as it can.
- Put it into `head`.

**Main logic**:

- If `head` is empty, call _initialPush_.
- If `head` is group start, call _groupOrTupleOps_.
- If `head` is unary operator, call _unaryOps_.
- If `buffer` is empty or `buffer.peek()` is group end,
  - If, stack is empty, we're done. Get your `head`. (Or return it to _groupOrTupleOps_.)
  - Otherwise, call _binaryOps_.
- If `buffer.peek()` is group start, call _callOps_.
- If `buffer.peek()` is comma, return `head` to _groupOrTupleOps_, saying 'this was tuple!'
- If `buffer.peek()` is `.`, `?.` or `::`, call _accessOps_.
- If `buffer.peek().precedence <= stack.peek().precedence`, (that is, `state for lookahead` in the
  table is 'descending' or 'equal') call _binaryOps_.
  - Note that, if `buffer.peek()` is identifier, it becomes infix call.
- Otherwise, call _push_.

Some implications:

- Operators with same precedence, like `1 + 2 + 3`, are grouped by order, like `(1 + 2) + 3`.
  However, this behavior can be easily changed. In 'main logic', we checked
  if `buffer.peek().precedence <= stack.peek().precedence` is true. Just checking if two precedences
  are equal suffices. Someone would want to ensure grouping expressions well, such
  as `[associative = false] operator fun plus()`.
- Generics are also parsed as _callOps_ like it were simple function; `myFunc<String>("123")` means
  calling type function `myFunc` with `String`, and calling the result of `myFunc<String>`
  with `"123"`. `MyClass<Type>` means calling type function `MyClass` with `Type`.
- Array accesses are also parsed as _callOps_, as all semantics are identical to normal function
  invocation except for group start/end operator.
- With extra modification, trailing lambda argument can be parsed as partial function call.
  In case of `myFunc(123) { println("hello") }`, function `myFunc(123)` is called with argument
  `{ println("hello") }`.
- Parsing local declaration should be easy; all declarations has hard keyword. Although it should be
  handled in tokenizer level. Tokenizer should throw `NotMatchedException.KeywordEncountered` in
  local context to signal this.

Required modifications:

- Finding the end of statement/expression; semicolon is not required for end of statement.
- Postfix operation needed to implement `?` (PropagateError)
