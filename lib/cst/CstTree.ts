import type { Span } from "../token/Span.ts";
import { GetSpanSymbol, type Spanned } from "../token/Spanned.ts";
import { Token } from "../token/Token.ts";
import { formatClass, FormatClassName } from "../utils/format.ts";
import { CstNode } from "./CstNode.ts";
import type { CstNodeInfo } from "./CstNodeInfo.ts";

export type CstTreeItem = CstTree | Token;

export abstract class CstTree<
  out Node extends CstNode = CstNode,
  Info extends CstNodeInfo<Node> = CstNodeInfo<Node>,
> implements Spanned {
  abstract node: Node;
  abstract readonly info: Info;

  abstract readonly source: CstTree<Node, Info>;

  abstract readonly isRead: boolean;
  abstract readonly isAttached: boolean;

  abstract readonly items: readonly CstTreeItem[];
  abstract readonly children: readonly CstTree[];
  abstract readonly tokens: readonly Token[];

  abstract readonly allSpans: readonly Spanned[];

  abstract readonly span: Span;

  // Used to support returning as-is parser
  abstract readonly shadowedGroups?: CstTree[];

  get [GetSpanSymbol](): Span {
    return this.span;
  }

  get allTokens(): readonly Token[] {
    const result: Token[] = [];
    for (const span of this.allSpans) {
      if (span instanceof Token) result.push(span);
      else if (span instanceof CstNode) result.push(...span.tree.allTokens);
      else {
        console.error("unknown span", span);
        throw new Error("unknown span");
      }
    }
    return result;
  }

  dump(): string {
    return formatClass({
      [FormatClassName]: this.constructor,
      node: this.node,
      span: this.span,
    });
  }
}
