package com.lhwdev.llang.cst.structure.declaration

import com.lhwdev.llang.cst.structure.CstNodeImpl
import com.lhwdev.llang.cst.structure.CstNodeInfo
import com.lhwdev.llang.cst.structure.core.CstIdentifier
import com.lhwdev.llang.cst.structure.core.CstModifiers
import com.lhwdev.llang.cst.structure.type.CstType
import com.lhwdev.llang.cst.structure.util.CstOptional


// will create corresponding class if needed
typealias CstMemberFunction = CstFunction
typealias CstLocalFunction = CstFunction
typealias CstConstructorFunction = CstFunction
typealias CstAccessorFunction = CstFunction

open class CstFunction(
	final override val annotations: CstAnnotations,
	
	val context: CstOptional<CstContextDeclaration>,
	
	final override val modifiers: CstModifiers,
	
	val kind: Kind,
	
	val extensionReceiverParameter: CstOptional<CstExtensionReceiverParameter>,
	
	final override val name: CstIdentifier,
	
	val typeParameters: CstOptional<CstTypeParameters>,
	
	val valueParameters: CstValueParameters,
	
	val returnType: CstOptional<CstType>,
	
	val typeParameterConstraints: CstOptional<CstTypeParameterConstraints>,
	
	val body: CstOptional<CstBody>,
) : CstNamedDeclaration, CstNodeImpl() {
	sealed class Kind {
		sealed class Standard : Kind()
		
		object ObjectMember : Standard() // system mostly treats global == object
		
		object ClassMember : Standard()
		
		object Local : Standard()
		
		
		object Constructor : Kind()
		
		
		object Accessor : Kind()
	}
	
	
	companion object Info : CstNodeInfo<CstFunction> {
		override fun dummyNode() = null
	}
}
