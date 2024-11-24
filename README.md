# llang

> A hard work of innocent student who haven't learned anything about parser/compiler.

Note that 'llang' is alias and I really couldn't come up with cool name.

Also see [book](book.md) and [syntax reference](syntax-reference.md).

**Useful Links:**

- [Parser](modules/tooling/parser/src/commonMain/kotlin/com/lhwdev/llang/parser/impl/parser.kt)
- [all tokens](modules/tooling/token/src/commonMain/kotlin/com/lhwdev/llang/token/TokenKinds.kt)

code -> cst(with token) -> ast -> fir -> ir

- code

  ``` kotlin
  val a: Int = query("lhwdev", 10 + 9)
  ```

- **cst**: target of code formatting. change of ast is applied here.
  This is merely a 'more structured token list'.

  Note that we merged 'tokenize' phase into 'Cst parsing phase'.
  Parsing tokens also requires some context (although less than cst),
  so I'm convinced that 'Why resolve same context two times? Just do
  everything at once.' As said earlier, cst is merely 'structured token
  list'. It's similar to the output of tokenizer, but more structured.

  ``` kotlin
  // Note that all tokens including whitespace are actually saved in CstNodes, but they are
  // saved separately by CstParseContext.
  CstLocalVariableDeclaration(
      modifiers = emptyList(),
      kind = listOf(CstLeafNode(valToken)),
      type = listOf(CstLeafNode(colonToken)),
      initializer = CstInitializer(
          equals = CstLeafNode(equalsToken),
          expression = CstCall(
              function = CstGetValue(CstIdentifier(queryToken)),
              valueArguments = listOf(
                  CstTuple(
                      open = CstLeafNode(parenOpenToken),
                      items = listOf(CstConstant.String("lhwdev"), commaToken, ...),
                      close = CstLeafNode(parenCloseToken),
                  )
              )
          )
      ),
  )
  
  // alternative declaration(including whitespace):
  CstLocalVariableDeclaration(
      modifiers = emptyList(),
      kind = listOf(valToken, whitespace),
      type = listOf(colonToken, whitespace2),
      initializer = listOf(
          whitespace3, equalsToken, whitespace4,
          CstCall(
              function = CstGetValue(CstIdentifier(queryToken)),
              valueArguments = listOf(
                  CstTuple(
                      open = parenOpenToken,
                      content = listOf(CstConstant.String("lhwdev"), commaToken, ...),
                      close = parenCloseToken,
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

- **Parsing**: **code string** --(lexing)--> **token** --(parsing)--> **cst** --(semantic
  parsing) --> **fir**
- **IDE/Code Highlighting**: ast -> annotations
- **IDE/Code Modification**: edited code -> (incremental) token -> ... (same as parsing)
- **Code Refactoring**: modify fir then apply to fir -> ast -> cst -> token
- **Diagnostics**: inspect ast / fir
- **Diagnostics Fix**: modify following ast / fir

## Incremental Parsing

Oops, see [book.md](book.md).
