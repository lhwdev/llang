package com.lhwdev.llang.vfs


/**
 * Old-school file interface. Existence of instance does not mean that file exists.
 */
interface VirtualFile {
	val name: String
	val nameWithoutExtension: String
	val extension: String
	
	val path: String
	
	val parent: VirtualFile
	
	operator fun get(childPath: String): VirtualFile
	operator fun get(vararg childPath: String): VirtualFile
	
	fun exists(): Boolean
	
	fun isFile(): Boolean
	fun isDirectory(): Boolean
	
	fun resolve(): VirtualFile
	
	suspend fun list(): List<VirtualFile>
	// suspend fun listLazy(): Flow<VirtualFile> // ? or Sequence?
	
	suspend fun openInput(exclusive: Boolean = true): Input
	suspend fun openOutput(): Output
	suspend fun openAppend(): Output
}
