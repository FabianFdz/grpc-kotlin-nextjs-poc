// :codegen-verify — proves generated Kotlin/Java compiles (ADR-2). Temporary:
// delete once E2/E3 wire generated/{java,kotlin} into the real services.
plugins {
    kotlin("jvm")
}

val grpcKotlinVersion = "1.4.1"
val grpcVersion = "1.65.1"
val protobufVersion = "3.25.3"
val coroutinesVersion = "1.8.1"

sourceSets {
    main {
        java.srcDir(rootDir.resolve("generated/java"))
        kotlin.srcDir(rootDir.resolve("generated/kotlin"))
    }
}

dependencies {
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("com.google.protobuf:protobuf-java:$protobufVersion")
    implementation("com.google.protobuf:protobuf-kotlin:$protobufVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // grpc/java output is @javax.annotation.Generated, not on the JDK 21
    // classpath (ADR-1).
    compileOnly("org.apache.tomcat:annotations-api:6.0.53")
}

// Guard: generated/{java,kotlin} package dirs must exist and be non-empty, or
// compileJava/compileKotlin would pass vacuously on empty source sets.
val expectedGeneratedDirs = listOf(
    "generated/java/com/poc/inventory/v1",
    "generated/java/com/poc/pricing/v1",
    "generated/kotlin/com/poc/inventory/v1",
    "generated/kotlin/com/poc/pricing/v1",
)

val verifyGeneratedSourcesPresent = tasks.register("verifyGeneratedSourcesPresent") {
    doFirst {
        val missing = expectedGeneratedDirs.filter { path ->
            val dir = rootDir.resolve(path)
            !dir.exists() || dir.listFiles().isNullOrEmpty()
        }
        if (missing.isNotEmpty()) {
            throw GradleException(
                "Missing generated sources: $missing. Run `pnpm generate` first."
            )
        }
    }
}

tasks.compileJava {
    dependsOn(verifyGeneratedSourcesPresent)
}

tasks.named("compileKotlin") {
    dependsOn(verifyGeneratedSourcesPresent)
}
