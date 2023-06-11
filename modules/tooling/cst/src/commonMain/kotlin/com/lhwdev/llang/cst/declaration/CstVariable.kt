package com.lhwdev.llang.cst.declaration


open class CstVariable : CstDeclaration


class CstStandaloneVariable : CstVariable

class CstMemberVariable : CstVariable, CstMemberDeclaration
