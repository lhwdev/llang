import com.lhwdev.build.*

plugins {
	kotlin("multiplatform")
	
	id("common-plugin")
}

commonConfig.kotlin {
	llangLibrary()
	
	dependencies {
		implementation(projects.tooling.token)
		implementation(projects.tooling.module)
		implementation(projects.utils)
	}
}
