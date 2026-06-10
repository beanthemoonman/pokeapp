plugins {
  alias(libs.plugins.kotlin.jvm)
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(17)
  }
}

dependencies {
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.javax.inject)

  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
}
