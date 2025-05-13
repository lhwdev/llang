import type { CstNode } from "../../cst/CstNode.ts";
import type { CstNodeInfo } from "../../cst/CstNodeInfo.ts";
import type { Spanned } from "../../token/Spanned.ts";
import type { CstMutableList, CstMutableListInternal } from "../CstMutableList.ts";
import type { CstIntermediateGroup } from "./CstIntermediateGroup.ts";

export interface CstParseIntrinsicsBase {
  insertChild<Node extends CstNode>(self: CstIntermediateGroup<any>, node: Node): Node;

  markNullable(): void;
  markDiscardable(): void;

  markVital(reason?: Spanned): void;

  intrinsicListCreated<T extends Spanned>(list: CstMutableListInternal<T>): CstMutableList<T>;
  intrinsicListPushItem<T extends Spanned>(list: CstMutableListInternal<T>, item: T): void;

  intrinsicTestNode(node: () => CstNode | boolean | null): boolean;

  provideImplicitNode(node: (() => CstNode | null) | null): void;

  intrinsic<T>(key: CstParseIntrinsicKey<T>): T;

  debugHint: {
    (key: "name", value: string): void;
    (key: "nodeName", value: string): void;
  };
}

export type CstParseIntrinsics<Info extends CstNodeInfo<any> = CstNodeInfo<any>> =
  & CstParseIntrinsicsBase
  & {
    intrinsic: Info["intrinsic"] extends CstParseIntrinsicKey<infer T> ? {
        (): T;
        <T>(key: CstParseIntrinsicKey<T>): T;
      }
      : (<T>(key: CstParseIntrinsicKey<T>) => T);
  }
  & GlobalIntrinsics<Info>;

type GlobalIntrinsics<Info extends CstNodeInfo<any>> = Info["intrinsic"] extends
  CstParseIntrinsicKey.Global<infer T> ? T : object;
export class CstParseIntrinsicKey<T> {
  declare private $parse_intrinsics: void;

  constructor(readonly name?: string) {}

  matches(other: CstParseIntrinsicKey<any>): other is CstParseIntrinsicKey<T> {
    return this === other;
  }
}

export namespace CstParseIntrinsicKey {
  export class Global<T> extends CstParseIntrinsicKey<T> {
    declare private $parse_intrinsics_global: void;
  }
}
