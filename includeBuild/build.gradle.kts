plugins {
	`kotlin-dsl`
	`java-gradle-plugin`
}


group = "com.lhwdev.include-build"

repositories {
	mavenCentral()
	google()
}

gradlePlugin {
	plugins.register("common-plugin") {
		id = "common-plugin"
		implementationClass = "com.lhwdev.build.CommonPlugin"
	}
}

dependencies {
	compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
}
