plugins {
    kotlin("jvm")
}

val grpcKotlinVersion = "1.4.1"
val grpcVersion = "1.65.1"
val protobufVersion = "3.25.3"
val coroutinesVersion = "1.8.1"
val ktorVersion = "2.3.12"
val junitVersion = "5.10.2"

dependencies {
    // gRPC + protobuf
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-netty:$grpcVersion")
    implementation("com.google.protobuf:protobuf-kotlin:$protobufVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // Ktor client for the DummyJSON third-party integration (external/ only).
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // Note: no compile-time dependency on :pricing-service — the internal
    // integration (PricingServiceClient) talks to it over the network via
    // gRPC, as a separate deployable process, not as a JVM library.

    // Tests
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// TODO: add the com.google.protobuf gradle plugin (or a sourceSets entry
// pointing at generated/kotlin/com/poc/inventory) once buf generate has been
// run — inventory-service only needs the inventory.v1 generated sources.

// TODO: add an `application` plugin block + mainClass (a Main.kt that starts
// the gRPC server) once InventoryGrpcService has a real implementation to
// serve.

tasks.test {
    useJUnitPlatform()
}
