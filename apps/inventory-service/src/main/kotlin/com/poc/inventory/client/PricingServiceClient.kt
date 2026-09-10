package com.poc.inventory.client

// TODO: implement a suspend function `getPrice(itemId: String): PriceInfo`
// that calls pricing-service's GetPrice RPC over internal gRPC (separate
// host/port from this service, configurable via env var, e.g.
// PRICING_SERVICE_ADDRESS, defaulting to something like
// pricing-service:50052).
//
// Must demonstrate:
//   - Coroutine-based async call (suspend fun using the generated
//     PricingCoroutineStub).
//   - A short timeout appropriate for an internal service call — shorter
//     than DummyProductClient's external-call timeout, since this is a
//     same-network, low-latency dependency and we want to fail fast rather
//     than let a slow pricing-service stall inventory responses.
//   - Graceful degradation: if pricing-service is unavailable or the call
//     times out, GetItemWithPricing (in service/InventoryGrpcService)
//     should still return the item without pricing data rather than
//     failing the whole RPC. Tradeoff to note explicitly: this favors
//     availability of the inventory read path over consistency/completeness
//     of the pricing data — acceptable here because pricing is
//     supplementary, not authoritative, for this RPC.
