import type { CstNode } from "../cst/CstNode.ts";
import type { Spanned } from "../token/Spanned.ts";
import { EmptySlot } from "./impl_old/CstIntermediateGroup.ts";
import { currentGroup } from "./intermediate/currentGroup.ts";
import type { CstContextLocal, CstContextLocalKey } from "./intermediate/CstContextLocal.ts";

export function memoize<T>(calculate: () => T, dependencies?: unknown[]): T {
  const current = currentGroup();
  if (dependencies) {
    const previous = current.nextSlot();
    if (previous === EmptySlot) return current.updateSlot(calculate());
    if (!(previous instanceof MemoizedValue)) throw new Error("whoa");

    if (shallowArrayEquals(dependencies, previous.dependencies)) return previous.value as T;
    return current.updateSlot(new MemoizedValue(calculate(), dependencies)).value as T;
  } else {
    const previous = current.nextSlot();
    if (previous === EmptySlot) return current.updateSlot(calculate());
    return previous as T;
  }
}

export namespace intrinsics {
  export function vital(reason?: Spanned) {
    currentGroup().intrinsics.markVital(reason);
  }

  export function testNode(fn: () => CstNode | null | boolean): boolean {
    return currentGroup().intrinsics.intrinsicTestNode(fn);
  }

  export function debugName(name: string) {
    currentGroup().intrinsics.debugHint("name", name);
  }

  export function debugNodeName(name: string) {
    currentGroup().intrinsics.debugHint("nodeName", name);
  }
}

export function insertNode<Node extends CstNode>(node: Node): Node {
  const self = currentGroup();
  return self.intrinsics.insertChild(self, node);
}

export function provideContext(value: CstContextLocal<any>): void {
  currentGroup().contextMap.provideContext(value);
}

export function useContext<T>(key: CstContextLocalKey<T>): T {
  return currentGroup().contextMap.resolveContext(key).value;
}

export function useImplicitNode(
  parser: (() => CstNode | null) | null,
): void {
  currentGroup().intrinsics.provideImplicitNode(parser);
}

/**
 * @deprecated discardable mode will be enabled by default in nullable node.
 */
export function enableDiscard() {
  // do nothing
}

export class MemoizedValue {
  constructor(
    readonly value: unknown,
    readonly dependencies: unknown[],
  ) {}
}

export function shallowArrayEquals(a: unknown[], b: unknown[]): boolean {
  if (a.length !== b.length) return false;
  for (let i = 0; i < a.length; i++) {
    if (a[i] !== b[i]) return false;
  }
  return true;
}
