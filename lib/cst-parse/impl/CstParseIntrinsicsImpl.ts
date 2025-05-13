import type { CstNode } from "../../cst/CstNode.ts";
import type { Spanned } from "../../token/Spanned.ts";
import type { CstMutableList, CstMutableListInternal } from "../CstMutableList.ts";
import {
  CstParseIntrinsicKey,
  type CstParseIntrinsics,
  type CstParseIntrinsicsBase,
} from "../intermediate/CstParseIntrinsics.ts";
import type { CstIntermediateState } from "./CstIntermediateState.ts";
import type { CstIntermediateMetadata } from "./CstIntermediateMetadata.ts";
import type { CstNodeInfo } from "../../cst/CstNodeInfo.ts";
import type { CstIntermediateGroup } from "../intermediate/CstIntermediateGroup.ts";
import { CstInsertedNode } from "../CstSpecialNode.ts";
import { CstIntermediateGroupBase } from "./CstIntermediateGroupBase.ts";
import { Contexts } from "./contexts.ts";
import { fmt } from "../../utils/format.ts";

export class CstParseIntrinsicsImpl<Info extends CstNodeInfo<any>>
  implements CstParseIntrinsicsBase {
  static create<Info extends CstNodeInfo<any> & { intrinsic?: CstParseIntrinsicKey<never> }>(
    info: Info,
    meta: CstIntermediateMetadata<Info>,
    state: CstIntermediateState<any>,
  ): CstParseIntrinsicsImpl<Info> & CstParseIntrinsics<Info>;
  static create<Info extends CstNodeInfo<any> & { intrinsic: CstParseIntrinsicKey<T> }, T>(
    info: Info,
    meta: CstIntermediateMetadata<Info>,
    state: CstIntermediateState<any>,
    value: T,
  ): CstParseIntrinsicsImpl<Info> & CstParseIntrinsics<Info>;
  static create<Info extends CstNodeInfo<any>>(
    info: Info,
    meta: CstIntermediateMetadata<Info>,
    state: CstIntermediateState<any>,
    value?: any,
  ): CstParseIntrinsicsImpl<Info> & CstParseIntrinsics<Info> {
    const key = info.intrinsic;
    if (key) {
      const impl = new CstParseIntrinsicsImpl(
        meta,
        state,
        (k) => k ? k === key ? value : null : null,
      );
      if (key instanceof CstParseIntrinsicKey.Global) {
        return Object.setPrototypeOf({ ...value }, impl);
      } else {
        return impl as any;
      }
    }
    return new CstParseIntrinsicsImpl(meta, state, () => null) as any;
  }

  constructor(
    readonly meta: CstIntermediateMetadata<Info>,
    readonly state: CstIntermediateState<any>,
    readonly getIntrinsic: <T>(key: CstParseIntrinsicKey<T> | null) => T | null,
  ) {}

  insertChild<Node extends CstNode>(self: CstIntermediateGroup<any>, node: Node): Node {
    return this.state.items.beginSpecialChild(
      self.as(CstIntermediateGroupBase),
      CstInsertedNode<Node>,
    ).buildNode((inserted) => {
      const result = inserted.intrinsics.insertNode(node);
      return inserted.end(new CstInsertedNode(result));
    }).value;
  }

  provideImplicitNode(node: (() => CstNode | null) | null): void {
    this.meta.provideContext(Contexts.ImplicitNode.provides(node));
  }

  markNullable(): void {
  }

  markDiscardable(): void {
  }

  markVital(reason?: Spanned): void {
    console.warn(`TODO: markVital does nothing; reason=${reason}`);
  }

  intrinsicListCreated<T extends Spanned>(list: CstMutableListInternal<T>): CstMutableList<T> {
    throw new Error("TODO");
  }

  intrinsicListPushItem<T extends Spanned>(list: CstMutableListInternal<T>, item: T): void {}

  intrinsicTestNode(node: () => CstNode | boolean | null): boolean {
    return !!node();
  }

  intrinsic<T>(key: CstParseIntrinsicKey<T>): T;
  intrinsic(): never;
  intrinsic<T>(key?: CstParseIntrinsicKey<T>): T {
    const intrinsic = this.getIntrinsic(key ?? null);
    if (!intrinsic) throw new Error(`could not find intrinsic for ${key}`);
    return intrinsic;
  }

  debugHint: CstParseIntrinsics["debugHint"] = function (key, value) {
    switch (key) {
      case "name":
        return;
      case "nodeName":
        return;
      default:
        console.warn(fmt`debugHint ${key} was ignored, value=${value}`.s);
    }
  };
}
