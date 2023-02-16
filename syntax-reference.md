# Syntax Reference

MOSTLY EQUAL TO KOTLIN YEAH

## Difference

- `var` = mutable, `val` = immutable
- no `null`, `Option` instead
- rust-like error handling, Result & Option + Try(? mark)
- immutability exists
- pure / side-effect function division exists
- types: `Int32` `Float64` `Boolean` `Byte`(= `Int8`)

## Difference in Compiler Api

- `someDeclaration.color`: Can abstract away things like `suspend`, `@Composable`
  * Can be utilized to reduce frontend burden
  * Hierarchy color like applier in composable
- `someDeclaration.implicitColor`: like pure(colorless) / impure(color),
  calls in place(colorless) / not(color)
