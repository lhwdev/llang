import type { CstNode } from "../../cst/CstNode.ts";
import type { CstNodeInfo } from "../../cst/CstNodeInfo.ts";
import { CstIntermediateGroup } from "../intermediate/CstIntermediateGroup.ts";
import type { CstIntermediateMetadata } from "./CstIntermediateMetadata.ts";
import type { CstIntermediateState } from "./CstIntermediateState.ts";

export abstract class CstIntermediateGroupBase<
  out Node extends CstNode,
  Info extends CstNodeInfo<Node> = CstNodeInfo<Node>,
> extends CstIntermediateGroup<Node, Info> {
  abstract readonly meta: CstIntermediateMetadata<Info>;
  abstract readonly state: CstIntermediateState<Node, Info>;
}
