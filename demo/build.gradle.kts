plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")

    // Apply the Application plugin to add support for building an executable JVM application.
    application
    
    // Kotlin serialization for JSON handling
    kotlin("plugin.serialization") version "1.9.22"
}

dependencies {
    // Project "demo" depends on project "nlp". (Project paths are separated with ":", so ":nlp" refers to the top-level "nlp" project.)
    implementation(project(":nlp"))
    
    // Ktor server dependencies
    implementation("io.ktor:ktor-server-core:2.3.12")
    implementation("io.ktor:ktor-server-netty:2.3.12")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    implementation("io.ktor:ktor-server-cors:2.3.12")
    implementation("io.ktor:ktor-server-html-builder:2.3.12")
    
    // HTTP client for external API calls
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-client-cio:2.3.12")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    
    // Kotlinx HTML for building HTML pages
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.9.1")
    
    // Kotlinx serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
}

application {
    // Define the Fully Qualified Name for the application main class
    mainClass = "sharepa.demo.StockDemoKt"
}