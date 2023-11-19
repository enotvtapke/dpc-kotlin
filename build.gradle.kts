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
  implementation("org.slf4j:slf4j-api:2.0.9")
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