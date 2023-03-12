package com.lhwdev.llang.ast.declaration

import com.lhwdev.llang.ast.AstNamed
import com.lhwdev.llang.ast.reference.AstTypeReference


interface AstValueParameter : AstDeclaration {
	val type: AstTypeReference?
	
	
	interface Simple : AstValueParameter, AstNamed {
		val isVararg: Boolean
		
		val isCrossinline: Boolean
		
		override val name: String
		
		override val type: AstTypeReference?
	}
	
	interface SimpleDeclared : Simple {
		override val type: AstTypeReference
	}
	
	interface SimpleLambda : Simple { // used as FullyInferable<SimpleLambda> -> nothing / only name / name + type
		@AstInferable
		override val type: AstTypeReference?
	}
	
	interface DispatchReceiver : AstValueParameter {
		override val type: AstTypeReference
	}
	
	interface ExtensionReceiver : AstValueParameter {
		override val type: AstTypeReference
	}
	
	
	interface ContextReceiver : AstValueParameter {
		override val type: AstTypeReference
	}
}
