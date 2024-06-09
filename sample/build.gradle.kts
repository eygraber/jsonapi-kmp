plugins {
  id("com.eygraber.conventions-kotlin-multiplatform")
  id("com.eygraber.conventions-android-library")
  id("com.eygraber.conventions-detekt")
  alias(libs.plugins.kotlinxSerialization)
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.eygraber.jsonapi.kmp.sample"
}

kotlin {
  allKmpTargets(
    project = project,
  )

  commonMainKspDependencies(project = project) {
    ksp(projects.ksp)
  }

  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.runtime)
        implementation(libs.kotlinx.serialization.json)
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
