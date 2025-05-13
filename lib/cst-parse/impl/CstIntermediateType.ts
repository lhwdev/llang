import { CstNode } from "../../cst/CstNode.ts";
import type { CstNodeInfo } from "../../cst/CstNodeInfo.ts";
import { CstImplicitNode } from "../CstSpecialNode.ts";
import { CstNodeType } from "../intermediate/CstNodeType.ts";
import { Contexts } from "./contexts.ts";
import type { CstIntermediateGroupBase } from "./CstIntermediateGroupBase.ts";
import {
  CstIntermediateMetadata,
  type CstTypedIntermediateMetadata,
} from "./CstIntermediateMetadata.ts";
import type { CstIntermediateState } from "./CstIntermediateState.ts";
import { CstParseIntrinsicsImpl } from "./CstParseIntrinsicsImpl.ts";

export class CstIntermediateType<out Info extends CstNodeInfo<any>> extends CstNodeType<Info> {
  declare private _metadataFactory?: CstTypedIntermediateMetadata<Info>;

  handleImplicit(
    self: CstIntermediateGroupBase<any>,
    // deno-lint-ignore no-unused-vars
    info: Info,
  ): boolean {
    const implicitFn = self.meta.resolveContextOrNull(Contexts.ImplicitNode)?.value;
    if (!implicitFn) return false;

    self.state.beginSpecialChild(self, CstImplicitNode).buildNullableNode(() => {
      const result = implicitFn();
      return result ? new CstImplicitNode(result) : null;
    });
    return true;
  }

  createMetadata(
    parent: CstIntermediateGroupBase<any>,
    info: Info,
    startOffset: number,
  ): CstIntermediateMetadata<Info> {
    if (!this._metadataFactory) {
      this._metadataFactory = CstIntermediateMetadata.defaultFactory(this);
    }
    return new this._metadataFactory(parent, info, startOffset);
  }

  createIntrinsics(
    info: Info,
    meta: CstIntermediateMetadata<Info>,
    state: CstIntermediateState<InstanceType<Info>, Info>,
  ) {
    return CstParseIntrinsicsImpl.create(info, meta, state);
  }

  static Default = new CstIntermediateType<typeof CstNode>(CstNode);
}
