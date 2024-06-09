plugins {
  id("com.eygraber.conventions-kotlin-multiplatform")
  id("com.eygraber.conventions-android-library")
  id("com.eygraber.conventions-detekt")
  id("com.eygraber.conventions-publish-maven-central")
  alias(libs.plugins.kotlinxSerialization)
}

android {
  namespace = "com.eygraber.jsonapi.kmp.core"
}

kotlin {
  allKmpTargets(
    project = project,
  )

  sourceSets {
    commonMain {
      dependencies {
        api(libs.kotlinx.serialization.json)
      }
    }

    commonTest {
      dependencies {
        implementation(kotlin("test"))
        implementation(kotlin("test-annotations-common"))
      }
    }
  }
}
