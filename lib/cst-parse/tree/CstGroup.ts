import type { CstNode } from "../../cst/CstNode.ts";
import type { CstNodeInfo } from "../../cst/CstNodeInfo.ts";
import { CstTree, type CstTreeItem } from "../../cst/CstTree.ts";
import type { Span } from "../../token/Span.ts";
import type { Spanned } from "../../token/Spanned.ts";
import type { Token } from "../../token/Token.ts";
import type { TokenKind } from "../../token/TokenKind.ts";

export class CstGroup<
  out Node extends CstNode,
  Info extends CstNodeInfo<Node> = CstNodeInfo<Node>,
> extends CstTree<Node> {
  override readonly node: Node;
  override readonly info: Info;
  override readonly source: CstTree<Node, Info>;
  override readonly isRead: boolean;
  override readonly isAttached: boolean;
  override readonly items: readonly CstTreeItem[];
  override readonly children: readonly CstTree<CstNode>[];
  override readonly tokens: readonly Token<TokenKind>[];
  override readonly allSpans: readonly Spanned[];
  override readonly span: Span;
  override shadowedGroups?: CstTree<CstNode>[] | undefined;
}

export class CstGroupMetadata<out Node extends CstNode> {
  readonly info: CstNodeInfo<Node>;
}
