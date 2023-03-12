package com.lhwdev.llang.common


interface Visibility {
	object Internal : DefaultVisibility()
	
	object Private : DefaultVisibility()
	
	object Protected : DefaultVisibility()
	
	object Public : DefaultVisibility()
	
	object Local : DefaultVisibility()
}


abstract class DefaultVisibility : Visibility
