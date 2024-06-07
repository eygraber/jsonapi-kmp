plugins {
  kotlin("jvm")
  id("com.eygraber.conventions-kotlin-library")
  id("com.eygraber.conventions-detekt")
}

dependencies {
  implementation(projects.annotations)

  implementation(libs.kotlinPoet.ksp)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.ksp)
}
