rootProject.name = "grpc-kotlin-nextjs-poc"

include(":inventory-service")
project(":inventory-service").projectDir = file("apps/inventory-service")

include(":pricing-service")
project(":pricing-service").projectDir = file("apps/pricing-service")
