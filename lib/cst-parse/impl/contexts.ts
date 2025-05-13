import type { CstNode } from "../../cst/CstNode.ts";
import { CstContextLocalKey } from "../intermediate/CstContextLocal.ts";

export namespace Contexts {
  export const ImplicitNode = new CstContextLocalKey<(() => CstNode | null) | null>("ImplicitNode");
}
