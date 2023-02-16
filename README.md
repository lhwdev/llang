# llang

Note that 'llang' is alias and I really couldn't come up with cool name.

code -> tokens -> cst -> ast -> fir -> ir

- code

  ``` kotlin
  val a: Int = query("lhwdev", 10 + 9)
  ```

- tokens

  ``` kotlin
  Keyword.Val
  Whitespace
  Identifier
  ...
  ```

- **cst**: target of code formatting. change of ast is applied here.

  ``` kotlin
  CstLocalVariableDeclaration(
      kind = CstLocalVariableDeclaration.Kind.Val(valToken),
      ws1 = wsToken1.ws,
      name = aToken,
      ws2 = null,
      type = CstDeclarationType(
          colon = colonToken,
          ws1 = wsTokenType1.ws,
          type = CstType(
              classifier = CstClassifierReference(listOf(intToken)),
          ),
          initializer = CstLocalVariableDeclaration.Initializer(
              ws1 = wsInitToken1.ws,
              equals = equalsToken,
              ws2 = wsInitToken2.ws,
              expression = (such complicated!),
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
