plugins {
    kotlin("jvm") version "2.3.20"
    application
}

group = "de.nekrozofbrionac.mahjong"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    //implementation("org.jetbrains.kotlinx:multik-kotlin:0.3.1")
    implementation("org.jetbrains.kotlinx:multik-openblas:0.3.1")
    // implementation("org.jetbrains.kotlinx:multik-default:0.3.1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
application {
    mainClass.set("de.nekrozofbrionac.mahjong.MainTrainKt")
}