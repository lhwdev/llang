package com.lhwdev.llang.ast.declaration

import com.lhwdev.llang.ast.AstNamed
import com.lhwdev.llang.ast.reference.AstTypeReference


interface AstValueParameter : AstDeclaration {
	val type: AstTypeReference
	
	
	interface Declared : AstValueParameter, AstNamed {
		val isVararg: Boolean
		
		val isCrossinline: Boolean
		
		override val name: String
		
		override val type: AstTypeReference
	}
	
	interface DispatchReceiver : AstValueParameter
	
	interface ExtensionReceiver : AstValueParameter
	
	interface ContextReceiver : AstValueParameter
}
