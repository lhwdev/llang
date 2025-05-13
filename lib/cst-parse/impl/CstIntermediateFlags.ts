export class CstIntermediateFlags {
  errorBehavior: "nullable" | "discardable" | false = false;
  isVital: boolean = false;

  get isErrorAcceptor(): boolean {
    return !!this.errorBehavior;
  }
}
