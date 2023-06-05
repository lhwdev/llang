package com.lhwdev.utils.reflect

import kotlin.reflect.KClass


// TODO: possible performance regression
fun KClass<*>.companionObject(): KClass<*>? = nestedClasses.find { it.isCompanion }
