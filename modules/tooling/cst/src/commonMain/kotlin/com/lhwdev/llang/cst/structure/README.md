# Cst Structure

**Cst Structure** is a high level representation of tokens tree. Cst structure itself does not
map 1:1 into all tokens. Instead, `CstManager` enables mapping between cst structure and cst raw
node tree.

There can be some 'implicit cst node' inside any cst nodes. When calling cst parser function such
as `cstExpression`, it calls other parser functions, but some results of them are not included in
resulting cst structure. In `CstTuple`, left paren(`(`), comma(`,`), right paren(`)`) and all
whitespaces are not included in `CstTuple` itself.
However, **raw cst node tree** includes all the implicit nodes.

## Mapping to Raw Cst Structure

```kotlin
context(CstManager)
val CstNode.rawTree: CstNodeTree
```

## Raw Cst Structure Interface


