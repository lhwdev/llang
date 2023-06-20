import com.lhwdev.build.*

plugins {
	kotlin("multiplatform")
	
	id("common-plugin")
}

commonConfig.kotlin {
	llangLibrary()
	
	dependencies {
		implementation(kotlin("reflect"))
		
		implementation(projects.tooling.module)
		implementation(projects.tooling.structureCommon)
		implementation(projects.tooling.token)
		implementation(projects.tooling.tokenizer)
		implementation(projects.tooling.parserCommon)
		implementation(projects.utils)
	}
}
