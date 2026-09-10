plugins {
    kotlin("jvm")
}

val grpcKotlinVersion = "1.4.1"
val grpcVersion = "1.65.1"
val protobufVersion = "3.25.3"
val coroutinesVersion = "1.8.1"
val junitVersion = "5.10.2"

dependencies {
    // gRPC + protobuf
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-netty:$grpcVersion")
    implementation("com.google.protobuf:protobuf-kotlin:$protobufVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // No ktor-client-* here: pricing-service makes no third-party HTTP calls.

    // Tests
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// TODO: add the com.google.protobuf gradle plugin (or a sourceSets entry
// pointing at generated/kotlin/com/poc/pricing) once buf generate has been
// run — pricing-service only needs the pricing.v1 generated sources.

// TODO: add an `application` plugin block + mainClass (a Main.kt that starts
// the gRPC server) once PricingGrpcService has a real implementation to
// serve.

tasks.test {
    useJUnitPlatform()
}
