# Syntax Reference

MOSTLY EQUAL TO KOTLIN YEAH

## Difference

- `var` = mutable, `val` = immutable
- no `null`, `Option` instead
- `++` is not expression, only statement. (but `++`/`--`is not implemented yet)
- rust-like error handling, Result & Option + Try(? mark)
- immutability exists
- pure / side-effect function division exists
- types: `Int32` `Float64` `Boolean` `Byte`(= `Int8`)
- arithmetic matches formal mathematical behavior
  * `-7 / 4` equals `-2 ... 1` so that remainder is always in `0..<divisor`.
    See [Euclidean division](https://en.wikipedia.org/wiki/Euclidean_division).
    (of course, to get division + remainder, you can use `divmod`, not `/`.)
  * `3 / 2` equals `1.5`, not `1`. `3 / 2` and `3.0 / 2.0` is identical.
    Note that `floor(3 / 2)` is well optimized.
  * Set-based type definition. For example, integer can always be used as real (number)
    without any distinction. (only exception is overload where `f(int)` and `f(real)`
    both exists.)
    So we don't call assigning int into real as 'type coercion', though it does exist in
    compiler backend.
  * ~~Index starts from 1. (of course this is joke)~~

## Difference in Compiler Api

- `someDeclaration.color`: Can abstract away things like `suspend`, `@Composable`
  * Can be utilized to reduce frontend burden
  * Hierarchy color like applier in composable
- `someDeclaration.implicitColor`: like pure(colorless) / impure(color),
  calls in place(colorless) / not(color)
