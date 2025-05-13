import type { CstNode } from "../../cst/CstNode.ts";
import type { CstNodeInfo } from "../../cst/CstNodeInfo.ts";
import type { CstSpecialNodeInfo } from "../CstSpecialNode.ts";
import type { CstIntermediateItem } from "../intermediate/CstIntermediateGroup.ts";
import type { CstIntermediateBehavior } from "./CstIntermediateBehavior.ts";
import type { CstIntermediateGroupBase } from "./CstIntermediateGroupBase.ts";
import type { CstIntermediateMetadata } from "./CstIntermediateMetadata.ts";
import { CstIntermediateType } from "./CstIntermediateType.ts";

export class CstIntermediateItems {
  constructor(
    protected readonly meta: CstIntermediateMetadata<any>,
    protected value: CstIntermediateItem[] = [],
  ) {}

  protected get behavior(): CstIntermediateBehavior {
    return this.meta.behavior;
  }

  protected acceptImplicit = false;

  get(): readonly CstIntermediateItem[] {
    return this.value;
  }

  protected findChildType<Info extends CstNodeInfo<any>>(_info: Info): CstIntermediateType<Info> {
    return CstIntermediateType.Default as any;
  }

  protected findSpecialChildType<Info extends CstNodeInfo<any>>(
    info: Info,
  ): CstIntermediateType<Info> {
    return this.behavior.findSpecialChildType(info);
  }

  beginChild<Info extends CstNodeInfo<any>>(
    self: CstIntermediateGroupBase<any>,
    info: Info,
  ): CstIntermediateGroupBase<InstanceType<Info>, Info> {
    console.assert(this === self.state.items, "this != self.items");

    const type = this.findChildType(info);
    type.handleImplicit(self, info);

    const meta = type.createMetadata(self, info, self.state.offset);
    const state = this.behavior.createIntermediateState<Info>(meta, self.state);
    const intrinsics = type.createIntrinsics(info, meta, state);
    return this.behavior.createIntermediateGroup(meta, state, intrinsics);
  }

  beginSpecialChild<Info extends CstSpecialNodeInfo<any>>(
    self: CstIntermediateGroupBase<any>,
    info: Info,
  ): CstIntermediateGroupBase<InstanceType<Info>, Info> {
    console.assert(this === self.state.items, "this != self.items");

    const type = this.findSpecialChildType(info);
    type.handleImplicit(self, info);

    const meta = type.createMetadata(self, info, self.state.offset);
    const state = this.behavior.createIntermediateState<Info>(meta, self.state);
    const intrinsics = type.createIntrinsics(info, meta, state);
    return this.behavior.createIntermediateGroup(meta, state, intrinsics);
  }

  endChild<Node extends CstNode>(
    child: CstIntermediateGroupBase<Node>,
    result: Node | CstErrorResult,
  ) {
    if (result instanceof CstErrorResult) {
      // ,
    } else {
      this.value.push(child);
    }
  }
}

export class CstErrorResult {
  constructor(readonly value: unknown | null) {}
}
