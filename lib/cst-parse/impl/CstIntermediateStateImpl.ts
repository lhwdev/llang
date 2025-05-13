import { detailedError } from "../../common/error.ts";
import type { CstNode } from "../../cst/CstNode.ts";
import type { CstNodeInfo } from "../../cst/CstNodeInfo.ts";
import { fmt } from "../../utils/format.ts";
import type { CstSpecialNodeInfo } from "../CstSpecialNode.ts";
import { CstGroup } from "../tree/CstGroup.ts";
import type { CstIntermediateBehavior } from "./CstIntermediateBehavior.ts";
import { CstIntermediateFlags } from "./CstIntermediateFlags.ts";
import type { CstIntermediateGroupBase } from "./CstIntermediateGroupBase.ts";
import { CstErrorResult, type CstIntermediateItems } from "./CstIntermediateItems.ts";
import type { CstIntermediateMetadata } from "./CstIntermediateMetadata.ts";
import type { CstIntermediateSlots } from "./CstIntermediateSlots.ts";
import { CstIntermediateState } from "./CstIntermediateState.ts";

export class CstIntermediateStateImpl<
  out Node extends CstNode,
  Info extends CstNodeInfo<Node> = CstNodeInfo<Node>,
> extends CstIntermediateState<Node, Info> {
  override offset: number;

  constructor(
    override readonly meta: CstIntermediateMetadata<any>,
    override readonly items: CstIntermediateItems,
    override readonly slots: CstIntermediateSlots,
    startOffset: number,
  ) {
    super();

    this.offset = startOffset;
  }

  protected group: CstGroup<Node, Info> | null = null;
  declare protected error?: unknown;

  declare flags?: CstIntermediateFlags;

  protected get behavior(): CstIntermediateBehavior {
    return this.meta.behavior;
  }

  protected ensureFlags(): CstIntermediateFlags {
    if (!this.flags) this.flags = new CstIntermediateFlags();
    return this.flags;
  }

  override beginChild<ChildInfo extends CstNodeInfo<any>>(
    self: CstIntermediateGroupBase<any>,
    info: ChildInfo,
  ): CstIntermediateGroupBase<InstanceType<ChildInfo>, ChildInfo> {
    return this.items.beginChild(self, info);
  }

  override beginSpecialChild<ChildInfo extends CstSpecialNodeInfo<any>>(
    self: CstIntermediateGroupBase<any>,
    info: ChildInfo,
  ): CstIntermediateGroupBase<InstanceType<ChildInfo>, ChildInfo> {
    return this.items.beginSpecialChild(self, info);
  }

  override skipCurrent(): Node | null {
    return null;
  }

  protected createGroup(node: Node): CstGroup<Node, Info> {
    return new CstGroup();
  }

  protected endSelf(self: CstIntermediateGroupBase<Node, Info>, result: Node | CstErrorResult) {
    this.parentState.items.endChild(self, result);
  }

  override beforeEnd(node: Node): CstGroup<Node, Info> {
    // node is not fully instantiated
    if (this.group || this.error) {
      const a = this.group?.info;
      if (!a) {
        throw detailedError`
          Creating multiple CstNode inside one node() call is not allowed.
          For example, ${fmt
          .code`new CstSimpleCall(new CstReference('hi'), [], [])`} is not allowed.
          To do this, use separate node() call, like ${
          fmt.code(`new CstSimpleCall(node(() => new CstReference('hi')), [], [])`)
        }.
        `;
      }
      const b = node.constructor;
      throw detailedError`
        Creating multiple CstNode inside one node() call is not allowed; tried to create \\
        ${b} while ${a} exists.
        For example, ${fmt.code`new ${b}(new ${a}('hi'), [], [])`} is not allowed.
        To do this, use separate node() call, like ${
        fmt.code(`new ${b}(node(() => new ${a}('hi')), [], [])`)
      }.
      `;
    }

    const group = this.createGroup(node);
    this.group = group;
    return group;
  }

  override end(self: CstIntermediateGroupBase<Node, Info>, node: Node): Node {
    let group = this.group;
    if (group) {
      if (group.node !== node) {
        throw detailedError`
          you should return CstNode that is newly created inside parser.
          - ${fmt.brightYellow`previously created`}: ${group.node}
          - ${fmt.brightYellow`given`}: ${node}
          - ${fmt.brightYellow`given.tree`}: ${node.tree}
        `;
      }
    } else {
      group = this.createGroup(node);
      this.group = group;

      // In this case, this parser returned node from child parser as-is.
      // We need special handling for this case.
      const childGroup = node.tree;
      const items = group.items;
      if (items.length > 1) {
        throw detailedError`
          to return node as-is from parser, you should call maximum of only one parser.
          - ${fmt.brightYellow`tree.items`}: ${items}
        `;
      }
      if (items.at(0) !== childGroup) {
        throw detailedError`
          to return node as-is from parser, you should call maximum of only one parser.
          - ${fmt.brightYellow`tree.items`}: ${items}
        `;
      }

      node.tree = group;
      if (group instanceof CstGroup) {
        group.shadowedGroups = [
          childGroup,
          ...childGroup.shadowedGroups ?? [],
        ];
      }
    }

    this.behavior.onEndGroup();
    this.endSelf(self, node);

    return node;
  }

  override endWithError(
    self: CstIntermediateGroupBase<Node, Info>,
    error: unknown | null,
  ): Node | null {
    if (error === undefined) {
      // TODO: ?
      throw new Error(
        fmt`${fmt.code`undefined`} error is used internally. Use other value.`.s,
      );
    }

    let result = null;
    this.error = error;

    const flags = this.flags;
    if (flags?.errorBehavior) {
      if (flags.errorBehavior === "nullable" && error !== null) {
        throw detailedError`Expected ${fmt.code`error == null`}, but got ${error}`;
      }
      this.behavior.onDiscardGroup();
    } else {
      this.behavior.onEndGroupForError();

      let parent = this.parentState;
      while (!parent.flags?.isErrorAcceptor) {
        const ancestor = parent.parentState;
        if (parent === ancestor) {
          // this error will go up to root
          // TODO: oh no!
          break;
        }
        parent = ancestor;
      }
    }

    this.endSelf(self, new CstErrorResult(error));
    return result;
  }
}
