plugins {
	kotlin("jvm")
	
	id("common-plugin")
}

commonConfig.kotlin {
	dependencies {
		implementation(projects.tooling.parser)
		implementation(projects.tooling.token)
		implementation(projects.tooling.module)
		implementation(projects.utils)
	}
}
