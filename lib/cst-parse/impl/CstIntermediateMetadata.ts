import type { CstNodeInfo } from "../../cst/CstNodeInfo.ts";
import type {
  CstContextLocal,
  CstContextLocalKey,
  CstProvidableContextLocalMap,
} from "../intermediate/CstContextLocal.ts";
import type { CstIntermediateBehavior } from "./CstIntermediateBehavior.ts";
import type { CstIntermediateGroupBase } from "./CstIntermediateGroupBase.ts";
import type { CstIntermediateType } from "./CstIntermediateType.ts";
import { CstProvidableContextLocalMapImpl } from "./CstProvidableContextLocalMapImpl.ts";

export abstract class CstIntermediateMetadata<Info extends CstNodeInfo<any>> {
  readonly contextMap: CstProvidableContextLocalMap;
  readonly behavior: CstIntermediateBehavior;

  constructor(
    readonly parent: CstIntermediateGroupBase<any>,
    readonly info: Info,
    readonly startOffset: number,
  ) {
    this.contextMap = new CstProvidableContextLocalMapImpl(this.parent.contextMap.source);
    this.behavior = parent.meta.behavior;
  }

  abstract readonly type: CstIntermediateType<Info>;

  resolveContext<T>(key: CstContextLocalKey<T>): CstContextLocal<T> {
    return this.contextMap.resolveContext(key);
  }
  resolveContextOrNull<T>(key: CstContextLocalKey<T>): CstContextLocal<T> | null {
    return this.contextMap.resolveContextOrNull(key);
  }
  provideContext(value: CstContextLocal<any>): void {
    return this.contextMap.provideContext(value);
  }

  static defaultFactory<Info extends CstNodeInfo<any>>(
    type: CstIntermediateType<Info>,
  ): CstTypedIntermediateMetadata<Info> {
    return class extends CstIntermediateMetadata<Info> {
      override get type(): CstIntermediateType<Info> {
        return type;
      }
    };
  }
}

declare class _CstTypedIntermediateMetadata<Info extends CstNodeInfo<any>>
  extends CstIntermediateMetadata<Info> {
  override readonly type: CstIntermediateType<Info>;
}

export type CstTypedIntermediateMetadata<Info extends CstNodeInfo<any>> =
  typeof _CstTypedIntermediateMetadata<Info>;
