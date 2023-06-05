import com.lhwdev.build.*

plugins {
	kotlin("multiplatform")
	
	id("common-plugin")
}

commonConfig.kotlin {
	llangLibrary()
}
