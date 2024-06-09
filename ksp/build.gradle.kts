plugins {
  kotlin("jvm")
  id("com.eygraber.conventions-kotlin-library")
  id("com.eygraber.conventions-detekt")
}

dependencies {
  implementation(projects.runtime)

  implementation(libs.kotlinPoet.ksp)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.ksp)
}
