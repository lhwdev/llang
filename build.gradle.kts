@Suppress("DSL_SCOPE_VIOLATION")
plugins {
	alias(libs.plugins.kotlinMultiplatform) apply false
	alias(libs.plugins.kotlinJvm) apply false
	alias(libs.plugins.kotlinSerialization) apply false
}

allprojects {
	repositories {
		google()
		mavenCentral()
	}
}
