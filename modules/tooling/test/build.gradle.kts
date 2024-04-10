plugins {
	kotlin("jvm")
	
	id("common-plugin")
}

commonConfig.kotlin {
	dependencies {
		implementation(projects.tooling.parser)
		implementation(projects.tooling.parserCommon)
		implementation(projects.tooling.tokenizer)
		implementation(projects.tooling.token)
		implementation(projects.tooling.cst)
		implementation(projects.tooling.module)
		implementation(projects.utils)
		
		implementation(kotlin("reflect"))
	}
}
