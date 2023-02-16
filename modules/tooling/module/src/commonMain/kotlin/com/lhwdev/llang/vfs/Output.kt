package com.lhwdev.llang.vfs


interface Output : Closeable {
	suspend fun write(data: ByteArray, offset: Int = 0, size: Int = data.size)
	
	suspend fun write(data: Byte) {
		write(byteArrayOf(data))
	}
}
