import type { CstNode } from "../cst/CstNode.ts";
import { CstPeekNode } from "./CstSpecialNode.ts";
import type { CstNodeInfo } from "../cst/CstNodeInfo.ts";
import { CstParserSymbol } from "./parser.ts";
import {
  currentGroup,
  intrinsicBeginGroup,
  intrinsicEndGroup,
} from "./intermediate/currentGroup.ts";

/// NOTE: if you update code of node() or nullableNode(), you should also modify parser.ts.

export function node<Node extends CstNode>(
  info: CstNodeInfo<Node>,
  fn: () => Node,
): Node {
  const parent = currentGroup();
  const child = parent.beginChild(info);
  intrinsicBeginGroup(child);
  try {
    const skip = child.skipCurrent();
    if (skip) return skip;

    const node = fn();
    return child.end(node);
  } catch (e) {
    const result = child.endWithError(e);
    if (!result) throw e;
    return result;
  } finally {
    intrinsicEndGroup(parent);
  }
}
node[CstParserSymbol] = true;

export function nullableNode<Node extends CstNode>(
  info: CstNodeInfo<Node>,
  fn: () => Node | null,
): Node | null {
  const parent = currentGroup();
  const child = parent.beginChild(info);
  intrinsicBeginGroup(child);
  try {
    const skip = child.skipCurrent();
    if (skip) return skip;

    child.intrinsics.markNullable();
    const node = fn();
    if (node) {
      return child.end(node);
    } else {
      return child.endWithError(null);
    }
  } catch (e) {
    const result = child.endWithError(e);
    if (!result) throw e;
    return result;
  } finally {
    intrinsicEndGroup(parent);
  }
}
nullableNode[CstParserSymbol] = true;

export function discardableNode<Node extends CstNode>(
  info: CstNodeInfo<Node>,
  fn: () => Node,
): Node | null {
  const parent = currentGroup();
  const child = parent.beginChild(info);
  intrinsicBeginGroup(child);
  try {
    const skip = child.skipCurrent();
    if (skip) return skip ;

    child.intrinsics.markDiscardable();
    const node = fn();
    return child.end(node);
  } catch (e) {
    const result = child.endWithError(e);
    if (!result) throw e;
    return result;
  } finally {
    intrinsicEndGroup(parent);
  }
}
discardableNode[CstParserSymbol] = true;

export function peek<R>(fn: () => R): R {
  return node(CstPeekNode<R>, () => new CstPeekNode(fn())).value;
}

export function peekNode<Node extends CstNode>(info: CstNodeInfo<Node>, fn: () => Node): Node {
  return node(CstPeekNode<Node>, () => new CstPeekNode(node(info, fn))).value;
}

export function peekNullable<Node extends CstNode>(
  info: CstNodeInfo<Node>,
  fn: () => Node | null,
): Node | null {
  return node(CstPeekNode<Node>, () => new CstPeekNode(nullableNode(info, fn))).value;
}
