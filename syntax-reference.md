# Syntax Reference

MOSTLY EQUAL TO KOTLIN YEAH

## Difference

- `var` = mutable, `val` = read-only, `const` = immutable
- no `null`, `Option` instead
- keyword `type` (replacement for `typealias` but superior..?)
- associated type, on top of generics like T.Error
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
  * Abstract algebra thingy like Ring.
  * ~~Index starts from 1. (of course this is joke)~~
- both compile-time and runtime generics exist
  * default is half compile-time, where `value class` becomes compile-time and other
    `class`(referential) becomes runtime
  * have full information about T by default even if T is referential class, like
    `value is T`, `T.Hello`.
  * you can do like `<erased T>` to force erased.
  * ~~and do `<inline T>` to optimize around.~~ just use value class to force this. If
    you use referential class, this is needless. `<inline T>` exists only for abi
    compatibility and explicitness.

## Difference in Compiler Api

- `someDeclaration.color`: Can abstract away things like `suspend`, `[composable]`
  * Can be utilized to reduce frontend burden
  * Can be included in top-down type inference;
    ``` kotlin
    [annotation()]
    object myColor
    fun hello(block: [myColor] () -> Unit)
    hello { /* <- this block has type of [myType] () -> Unit */ }
    ```

    Note that most annotations are not inferred top-down, instead they are ignored.
  * ex: Hierarchy color like applier in composable
- `someDeclaration.implicitColor`: like pure(colorless) / impure(color),
  calls in place(colorless) / not(color)
