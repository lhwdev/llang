package com.lhwdev.utils.collection


class CollectionRange(val start: Int, val end: Int) {
	init {
		require(start <= end) { "start($start) > end($end)" }
	}
	
	val endInclusive: Int
		get() = end - 1
	
	fun copy(start: Int = this.start, end: Int = this.end): CollectionRange =
		CollectionRange(start, end)
	
	
	operator fun component1(): Int = start
	
	operator fun component2(): Int = end
	
	override fun equals(other: Any?): Boolean = when {
		this === other -> true
		other !is CollectionRange -> false
		else -> start == other.start && end == other.end
	}
	
	override fun hashCode(): Int =
		start * 31 + end
	
	override fun toString(): String = "$start..<$end"
}
