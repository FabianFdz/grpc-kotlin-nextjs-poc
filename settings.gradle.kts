rootProject.name = "grpc-kotlin-nextjs-poc"

include(":inventory-service")
project(":inventory-service").projectDir = file("apps/inventory-service")

include(":pricing-service")
project(":pricing-service").projectDir = file("apps/pricing-service")

// Temporary — verifies generated/{java,kotlin} compile (ADR-2). Delete when
// E2/E3 wire generated sources into the real services.
include(":codegen-verify")
project(":codegen-verify").projectDir = file("tools/codegen-verify")
