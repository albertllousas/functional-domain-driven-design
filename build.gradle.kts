import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.20"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    application
}

repositories {
    jcenter()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(group = "io.arrow-kt", name = "arrow-core", version = "0.11.0")
    implementation(group = "io.arrow-kt", name = "arrow-core-data", version = "0.11.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.+")

    testImplementation("org.assertj:assertj-core:3.18.1")
    testImplementation("io.mockk:mockk:1.10.3")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation(group = "com.github.javafaker", name = "javafaker", version = "1.0.2")
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions.freeCompilerArgs += arrayOf("-Xinline-classes")

application {
    mainClass.set("com.alo.loan.AppKt")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xinline-classes")
    }
}

tasks.apply {
    test {
        enableAssertions = true
        useJUnitPlatform {}
    }

    task<Test>("unitTest") {
        description = "Runs unit tests."
        useJUnitPlatform {
            excludeTags("integration")
            excludeTags("component")
        }
        shouldRunAfter(test)
    }

    task<Test>("integrationTest") {
        description = "Runs integration tests."
        useJUnitPlatform {
            includeTags("integration")
        }
        shouldRunAfter(test)
    }

    task<Test>("componentTest") {
        description = "Runs component tests."
        useJUnitPlatform {
            includeTags("component")
        }
        shouldRunAfter(test)
    }
}
