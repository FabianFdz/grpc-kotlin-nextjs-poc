package com.poc.inventory.external

// TODO: implement a suspend function `fetchProduct(id: Int): DummyProduct`
// that calls GET https://dummyjson.com/products/{id} via Ktor Client
// (CIO engine), configured through ExternalClientConfig.
//
// Must demonstrate:
//   - Coroutine-based async call (suspend fun, no blocking calls).
//   - Explicit timeout configuration (HttpTimeout plugin) — this is the
//     external-call timeout, and should be configured longer than
//     PricingServiceClient's internal-call timeout since third-party APIs
//     are expected to be slower/less reliable than internal services.
//   - Retry with exponential backoff (max 3 attempts) on 5xx responses and
//     on timeout/connection failures. No retry on 4xx (client errors, e.g.
//     404) — those are terminal.
//   - Mapping HTTP outcomes to a domain-level sealed result instead of
//     letting exceptions leak into the gRPC layer, e.g.:
//       sealed interface DummyProductResult {
//           data class Success(val product: DummyProduct) : DummyProductResult
//           data object NotFound : DummyProductResult
//           data object ExternalServiceUnavailable : DummyProductResult
//       }
//     EnrichItem (in service/InventoryGrpcService) should branch on this
//     result rather than catching exceptions itself.
