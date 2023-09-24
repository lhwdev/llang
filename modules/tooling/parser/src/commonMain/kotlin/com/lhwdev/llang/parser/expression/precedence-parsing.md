Precedence parsing in book.md, but fit into llang system.

**In _binaryOps_**:

- Put `expressions.pop()` into `lhs`.
- Put `operators.pop()` into `operation`.
- Combine with `head`(`rhs`) to become a binary operation.
- Put it into `head`.

**In _unaryOps_**:

- Let `operator` be `head`.
- Assign `buffer.pop()` into `head`.
- Call

**In _accessOps_**:

- Combine `head`(lhs), `buffer.pop()`(dot etc.), `buffer.pop()`(rhs) into access operation, to
  become `lhs.rhs`, `lhs?.rhs` or `lhs::rhs`.
- Put it into `head`.

**In _callOps_**:

- Save `head` into `function`.
- Put `buffer.pop()`(group start) into `head`.
- Run _groupOrTupleOps_ with 'tuple mode'. (Saying 'this was tuple!' in advance)
- Combine `function` and `head`(tuple) into function invocation.
- Assign it into `head`.

**In _groupOrTupleOps_**:

- Run new fresh parsing with `buffer`. (Note that `buffer` does not contain group start)
- If the result says 'this was tuple!', should I write how to parse tuple?
- Combine `head`(group start), result of 'new fresh parsing', `buffer.pop()`(group end) into a
  group. Note that 'new fresh parsing' already consumed buffer as much as it can.
- Put it into `head`.

**In _expandHeadEager_**:

- If `head` is LeftParen, call _groupOrTupleOps_.
- If `head` is LeftBrace, parse as lambda function. (TODO)
- If `head` is unary operator, call _unaryOps_.
- If `buffer.peek()` is group start, call _callOps_.
- If `buffer.peek()` is comma, return `head` to _groupOrTupleOps_, saying 'this was tuple!'
- If `buffer.peek()` is `.`, `?.` or `::`, call _accessOps_.
- Return resulting expression if available.

**Main logic**:

- If `buffer` is empty or `buffer.peek()` is group end,
  - If, expression stack is empty, we're done. Get your `head`.
  - Otherwise, call _binaryOps_.
- If _expandHeadEager_ returns expression, return.
- Push `buffer.pop()` into `operators`.
- If `buffer.peek().precedence <= stack.peek().precedence`, (that is, `state for lookahead` in the
  table is 'descending' or 'equal') call _binaryOps_.
  - Note that, if `buffer.peek()` is identifier, it becomes infix call.
- Otherwise, call _push_.
