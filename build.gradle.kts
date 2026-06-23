plugins {
    kotlin("jvm") version "2.3.21"
    id("com.gradleup.shadow") version "9.4.2"
}

group = "me.anderctrl"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://snapshots.kord.dev")
}
dependencies {
    implementation("dev.kord:kord-core:0.18.1")
    implementation("dev.schlaubi.lavakord:kord:9.2.0")

    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")

    implementation("org.slf4j:slf4j-api:2.0.13")

    implementation("org.apache.logging.log4j:log4j-core:2.25.4")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")

    implementation("net.minecrell:terminalconsoleappender:1.3.0")
}

kotlin {
    jvmToolchain(21)
}

tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "me.anderctrl.luteciokt.MainKt"
    }
}

tasks.test {
    useJUnitPlatform()
}