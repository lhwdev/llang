# llang

Note that 'llang' is alias and I really couldn't come up with cool name.  
Llang is my toy project and now work in progress. As a developer, one should have thought, 'I want to write programming
language!' and that's it.

Also see [book](book.md) and [syntax reference](syntax-reference.md).

**Useful Links:**

- [Lexer](modules/tooling/lexer/src/commonMain/kotlin/com/lhwdev/llang/lexer/lexer.kt)
- [all tokens](modules/tooling/token/src/commonMain/kotlin/com/lhwdev/llang/token/TokenKinds.kt)

code -> tokens -> cst -> ast -> fir -> ir

- code

  ``` kotlin
  val a: Int = query("lhwdev", 10 + 9)
  ```

- **tokens**

  ``` kotlin
  Keyword.Val
  Whitespace
  Identifier
  ...
  ```

  In case of lexing, we need extra validation, such as 'if some tokens are adjacent?'.
  In the following code: `1234abcd` we get two valid tokens: `1234` and `abcd`. But we should mark `abcd` as invalid
  token. So we run post lexing validation.
  There are 'separator' tokens, like Operation, Whitespace, Eol, etc. The all
  non-separator tokens should not be adjacent. Quite simple? (TODO: check if this is true)  
  Note: enable this in IC

- **cst**: target of code formatting. change of ast is applied here.
  This is merely a 'more structured token list'.

  ``` kotlin
  CstLocalVariableDeclaration(
      modifiers = emptyList(),
      kind = listOf(valToken, whitespace),
      type = listOf(colonToken, whitespace2),
      initializer = listOf(
          whitespace3, equalsToken, whitespace4,
          CstCall(
              function = CstGetValue(queryToken),
              valueArguments = listOf(
                  CstGroup(
                      open = parenOpenToken,
                      content = listOf(CstConstant.String("lhwdev"), commaToken, ...)
                  )
              )
          )
      ),
  )
  ```

- **ast**: target of code refactoring. directly linked to fir.

  ``` kotlin
  AstLocalVariableDeclaration(
      isVar = false,
      modifier = listOf(),
      name = Identifier("a"), // (such a thing like this is stub)
      declaredType = AstTypeReference(AstClassifierReference("Int")),
      initializer = AstCall(
          function = AstGetValue(AstReference("query")),
          typeArguments = emptyList(),
          valueArguments = listOf(
              AstConst.String("lhwdev"),
              AstBinaryOps(AstBinaryOperator.Plus, AstConst.IntegerKind(10), AstConst.IntegerKind(9)),
          ),
      ),
  ```

- **fir**: target of semantic analysis and diagnostics.
  ast -> fir is called 'semantic analysis'.

  Fir should be fully analyzed, but ast can be partially replaced with firs, which
  means if you need semantically partially-analyzed ast, just get ast and get fir
  where you really need it.

  ``` kotlin
  FirLocalVariable( // ...,
      name = FirVariableSymbol("a", ...),
      type = FirType(FirClassifier(intSymbol), typeArguments = emptyList()),
      initializer = FirCall(
          function = FirGetValue(querySymbol),
          typeArguments = emptyList(),
          valueArguments = listOf(
              FirConst.String("lhwdev"),
              FirBinaryOps(FirCall(
                  function = FirGetValue(intPlusSymbol),
                  receiver = intSymbol,
                  typeArguments = emptyList(),
                  valueArguments = listOf(FirConst.Int(10), FirConst.Int(9)),
              )),
          )
      )
  ```

- **ir**: target of backend compilation and optimization. will be converted into
  some kind of binary code, or something cool.

  ``` kotlin
  IrLocalVariable( // ...,
      name = IrVariableSymbol("a", ...),
      type = IrType(IrClassifier(intSymbol), typeArguments = emptyList()),
      initializer = IrCall(
          function = IrGetValue(querySymbol),
          typeArguments = emptyList(),
          valueArguments = listOf(
              IrConst.String("lhwdev"),
              IrCall(
                  function = IrGetValue(intPlusSymbol),
                  receiver = intSymbol,
                  typeArguments = emptyList(),
                  valueArguments = listOf(IrConst.Int(10), IrConst.Int(9)),
                  origin = IrCallOrigin.PlusOperator,
              ),
          ),
      ),
  )
  ```

## Basic Architecture

### Reference

- **cst --> token** as cst itself is full of token.
- **ast --> cst** for reconstructing code from ast.
- **ast --(resolver)--> fir** in diagnostics or intermediate frontend compiler plugin (lazy)
- **fir --> ast** in code refactoring to apply code changes
- **ir --> fir** for knowing declaration structure from IR  
  Note that fir is used to declare structures of declaration, like Descriptor in Kotlin IR.
- **IrSymbol --> IrDeclarationStructure**, but following IrDeclaration
  may be a stub, which does not have bodies. You can get bodies of any valid symbols
  in linking stage later.

### Operations

- **Parsing**: **code string** --(lexing)--> **token** --(parsing)--> **cst** --(semantic parsing)-->
  **fir**
- **IDE/Code Highlighting**: ast -> annotations
- **IDE/Code Modification**: edited code -> (incremental) token -> ... (same as parsing)
- **Code Refactoring**: modify fir then apply to fir -> ast -> cst -> token
- **Diagnostics**: inspect ast / fir
- **Diagnostics Fix**: modify following ast / fir

## Incremental Parsing

Oops, see [book.md](book.md).
