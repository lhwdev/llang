package com.lhwdev.llang.module

import com.lhwdev.llang.vfs.VirtualFile


interface LlangFile {
	val name: String
	
	val virtualFile: VirtualFile
	
	suspend fun code(invalidate: Boolean = false): LlangCode
}
