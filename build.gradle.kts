plugins {
  kotlin("jvm") version "1.9.0"
  application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation("guru.nidi:graphviz-java:0.18.1")
  implementation("org.apache.logging.log4j:log4j-api:2.22.0")
  implementation("org.apache.logging.log4j:log4j-core:2.22.0")
  implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.21.1")
  testImplementation("org.assertj:assertj-core:3.25.3")
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}

kotlin {
  jvmToolchain(8)
}

application {
  mainClass.set("MainKt")
}