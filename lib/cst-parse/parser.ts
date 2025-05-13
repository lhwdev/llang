import { yellow } from "../utils/ansi.ts";
import type { CstNode } from "../cst/CstNode.ts";
import type { CstNodeInfo } from "../cst/CstNodeInfo.ts";
import { node, nullableNode } from "./inlineNode.ts";
import { ParseError } from "./parseError.ts";
import type { CstIntermediateGroup } from "./intermediate/CstIntermediateGroup.ts";
import {
  currentGroup,
  intrinsicBeginGroup,
  intrinsicEndGroup,
  withGroup,
} from "./intermediate/currentGroup.ts";

export const CstParserSymbol = Symbol("CstParserSymbol");

abstract class CstParserClass<Node extends CstNode, Fn extends (...args: any[]) => any> {
  get [CstParserSymbol](): true {
    return true;
  }

  abstract info: CstNodeInfo<Node>;

  invoke(...args: Parameters<Fn>): ReturnType<Fn> {
    return this.surround(() => this.invokeRaw(...args));
  }

  protected abstract surround(inner: () => ReturnType<Fn>): ReturnType<Fn>;
  abstract invokeRaw(...args: Parameters<Fn>): ReturnType<Fn>;

  invokeWith(
    self: CstIntermediateGroup<any>,
    ...args: Parameters<Fn>
  ): ReturnType<Fn> {
    return withGroup(self, () => this.invoke(...args));
  }

  invokeWithInside(
    extra: (fn: () => ReturnType<Fn>) => ReturnType<Fn>,
    ...args: Parameters<Fn>
  ): ReturnType<Fn> {
    return this.surround(() => extra(() => this.invokeRaw(...args)));
  }

  nonNull(...args: Parameters<Fn>): NonNullable<ReturnType<Fn>> {
    // todo: remove nullable flag
    const node = this.invoke(...args);
    if (node === null || node === undefined) {
      throw new ParseError("result is null");
    }
    return node;
  }
}

export const CstParser = CstParserClass;
export type CstParser<Node extends CstNode, Fn extends (...args: any) => any> =
  & CstParserClass<Node, Fn>
  & Fn;

export interface ParserOptions {
  name?: string;
}

export function rawParserInit(
  info: CstNodeInfo<any>,
  impl: (...args: any) => any,
  options: ParserOptions = {},
) {
  function parserName(option: string | undefined, info: CstNodeInfo<any>): string {
    const extractName = () => {
      const name = info.name;
      if (!name.length) return "";
      return name[0].toLowerCase() + name.slice(1);
    };
    return yellow(option ?? extractName());
  }

  Object.defineProperty(impl, "name", { value: parserName(options.name, info) });
}

export function rawParser<Node extends CstNode, Params extends any[], Return>(
  info: CstNodeInfo<Node>,
  surround: (inner: () => Return) => Return,
  impl: (...args: Params) => Return,
  options?: ParserOptions,
): CstParser<Node, (...args: Params) => Return> {
  rawParserInit(info, impl, options);
  const parser = new class extends CstParser<Node, (...args: Params) => Return> {
    override info = info;
    override invokeRaw = impl;
    override surround = surround;
  }();
  return Object.setPrototypeOf((...args: Params) => parser.invoke(...args), parser);
}

export function parser<Params extends any[], Node extends CstNode>(
  info: CstNodeInfo<Node>,
  impl: (...args: Params) => Node,
  options?: ParserOptions,
): CstParser<Node, ((...args: Params) => Node)> {
  rawParserInit(info, impl, options);
  const invoke = (...args: Params): Node => {
    const parent = currentGroup();
    const child = parent.beginChild(info);
    intrinsicBeginGroup(child);
    try {
      const skip = child.skipCurrent();
      if (skip) return skip;

      const node = impl(...args);
      return child.end(node);
    } catch (e) {
      const result = child.endWithError(e);
      if (!result) throw e;
      return result;
    } finally {
      intrinsicEndGroup(parent);
    }
  };
  const parser = new class extends CstParser<Node, ((...args: Params) => Node)> {
    override info = info;
    override invoke = invoke;
    override invokeRaw = impl;
    override surround = (inner: () => Node) => node(info, inner);
  }();
  return Object.setPrototypeOf(Object.assign(invoke, parser), Object.getPrototypeOf(parser));
}

export function nullableParser<Params extends any[], Node extends CstNode>(
  info: CstNodeInfo<Node>,
  impl: (...args: Params) => Node | null,
  options?: ParserOptions,
): CstParser<Node, ((...args: Params) => Node | null)> {
  rawParserInit(info, impl, options);
  const invoke = (...args: Params): Node | null => {
    const parent = currentGroup();
    const child = parent.beginChild(info);
    intrinsicBeginGroup(child);
    try {
      const skip = child.skipCurrent();
      if (skip) return skip;

      child.intrinsics.markNullable();
      const node = impl(...args);
      if (node) {
        return child.end(node);
      } else {
        return child.endWithError(null);
      }
    } catch (e) {
      const result = child.endWithError(e);
      if (!result) throw e;
      return result;
    } finally {
      intrinsicEndGroup(parent);
    }
  };
  const parser = new class extends CstParser<Node, ((...args: Params) => Node | null)> {
    override info = info;
    override invoke = invoke;
    override invokeRaw = impl;
    override surround = (inner: () => Node | null) => nullableNode(info, inner);
  }();
  return Object.setPrototypeOf(Object.assign(invoke, parser), Object.getPrototypeOf(parser));
}
