package com.lhwdev.llang.ast.declaration

import com.lhwdev.llang.ast.AstNamed
import com.lhwdev.llang.ast.type.AstType


interface AstValueParameter : AstDeclaration {
	val type: AstType?
	
	
	interface Simple : AstValueParameter, AstNamed {
		val isVararg: Boolean
		
		val isCrossinline: Boolean
		
		override val name: String
		
		override val type: AstType?
	}
	
	interface SimpleDeclared : Simple {
		override val type: AstType
	}
	
	interface SimpleInferred : Simple { // used as FullyInferable<SimpleLambda> -> nothing / only name / name + type
		@AstInferable
		override val type: AstType?
	}
	
	interface DispatchReceiver : AstValueParameter {
		override val type: AstType
	}
	
	interface ExtensionReceiver : AstValueParameter {
		override val type: AstType
	}
	
	
	interface ContextReceiver : AstValueParameter {
		override val type: AstType
	}
}
