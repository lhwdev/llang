package com.lhwdev.llang.vfs


interface Input : Closeable {
	/**
	 * -1 means 'cannot estimate remaining', 0 means eof.
	 */
	val remaining: Int
	
	/**
	 * If eof, silently returns 0.
	 * @return count of bytes read into [data].
	 */
	suspend fun read(data: ByteArray, offset: Int = 0, size: Int = data.size): Int
}
