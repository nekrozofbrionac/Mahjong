plugins {
    kotlin("jvm") version "2.0.20"
    application
}

group = "de.nekrozofbrionac.mahjong"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
application {
    mainClass.set("de.nekrozofbrionac.mahjong.MainKt")
}