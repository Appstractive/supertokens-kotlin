import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin-platform-jvm")
    id("io.ktor.plugin") version libs.versions.ktor.version
    kotlin("plugin.serialization")
}

version = "1.0.0"
group = "com.supertokens.backend"

dependencies {
    implementation(projects.common)
    implementation(projects.sdk.backend)

    implementation(libs.kotlin.serialization)
    implementation(libs.kotlin.coroutines)

    implementation(libs.ktor.serialization)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.contentnegotiation)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.json)
    implementation(libs.ktor.client.logging)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.contentnegotiation)
    implementation(libs.ktor.server.statuspages)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)

    implementation(libs.slf4j)
    implementation(libs.jwt)

    testImplementation(libs.test.kotlin)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}