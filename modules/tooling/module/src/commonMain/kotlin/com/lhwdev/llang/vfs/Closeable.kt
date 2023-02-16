package com.lhwdev.llang.vfs


interface Closeable {
	/**
	 * Calling close several times is allowed, and following calls will be ignored.
	 */
	suspend fun close()
}
