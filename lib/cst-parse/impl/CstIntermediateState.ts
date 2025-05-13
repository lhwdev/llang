import type { CstNode } from "../../cst/CstNode.ts";
import type { CstNodeInfo } from "../../cst/CstNodeInfo.ts";
import type { CstSpecialNodeInfo } from "../CstSpecialNode.ts";
import type { CstGroup } from "../tree/CstGroup.ts";
import type { CstIntermediateFlags } from "./CstIntermediateFlags.ts";
import type { CstIntermediateGroupBase } from "./CstIntermediateGroupBase.ts";
import type { CstIntermediateItems } from "./CstIntermediateItems.ts";
import type { CstIntermediateMetadata } from "./CstIntermediateMetadata.ts";
import type { CstIntermediateSlots } from "./CstIntermediateSlots.ts";

export abstract class CstIntermediateState<
  out Node extends CstNode,
  Info extends CstNodeInfo<Node> = CstNodeInfo<Node>,
> {
  abstract readonly meta: CstIntermediateMetadata<any>;
  abstract readonly items: CstIntermediateItems;
  abstract readonly slots: CstIntermediateSlots;

  abstract readonly flags?: CstIntermediateFlags;

  abstract readonly offset: number;

  get parentState(): CstIntermediateState<any> {
    return this.meta.parent.state;
  }

  abstract beginChild<ChildInfo extends CstNodeInfo<any>>(
    self: CstIntermediateGroupBase<any>,
    info: ChildInfo,
  ): CstIntermediateGroupBase<InstanceType<ChildInfo>, ChildInfo>;

  abstract beginSpecialChild<ChildInfo extends CstSpecialNodeInfo<any>>(
    self: CstIntermediateGroupBase<any>,
    info: ChildInfo,
  ): CstIntermediateGroupBase<InstanceType<ChildInfo>, ChildInfo>;

  abstract skipCurrent(): Node | null;

  abstract beforeEnd(node: Node): CstGroup<Node, Info>;

  abstract end(self: CstIntermediateGroupBase<Node, Info>, node: Node): Node;

  abstract endWithError(
    self: CstIntermediateGroupBase<Node, Info>,
    error: unknown | null,
  ): Node | null;
}
