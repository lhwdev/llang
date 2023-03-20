package com.lhwdev.llang.ast.type

import com.lhwdev.llang.ast.AstNode
import com.lhwdev.llang.ast.type.AstVariance.*


/**
 * Parameters for functions and variables have their own variance([In] for function parameter, [Out] for function
 * returns, [Exact] for mutable variable, [Out] for readable variable.), but type parameters can have a various
 * variance.
 */
interface AstVariance : AstNode {
	interface Exact : AstVariance {
		val type: AstType
	}
	
	/**
	 * 1. Type parameter like `class Class<in T>` (`Class<Child> : Class<Parent>`)
	 *    If you have `Class<Animal>`, it should ensure you can input some member function with any instance of `Animal`.
	 *    If you do like `const other: Class<Dog> = instance`, you limit the types you can input.
	 *
	 * 2. Type argument like `fun hello(param: Class<in T>)`
	 *    This has a same semantics with 1.
	 *
	 * 3. Projection merging
	 *    `in` on parameter and `in` on argument counts the same. If both exists it will warn you about being redundant,
	 *    but it does not conflict. `in` and `out` cannot both exists on one type parameter.
	 */
	interface In : AstVariance {
		val type: AstType
	}
	
	/**
	 * like `class Class<out T>` (`Class<Parent> : Class<Child>`)
	 * If you have `Class<Dog>`, you can abstract the output type as `Class<Animal>`, but you cannot know if output type
	 * is Maltese or anything, so you cannot assign it to `val hello: Class<Maltese>`.
	 */
	interface Out : AstVariance {
		val type: AstType
	}
	
	interface StarProjection : AstVariance
}
