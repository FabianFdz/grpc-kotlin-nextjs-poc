package com.poc.inventory.external

// TODO: test DummyProductClient using Ktor's MockEngine (no real network
// calls). This is the only test file in the skeleton — CRUD and the
// pricing client aren't tested here since they don't demonstrate anything
// beyond what's already covered.
//
// Cases to cover once DummyProductClient is implemented:
//   - 200 response -> maps to DummyProductResult.Success with the parsed
//     DummyProduct.
//   - 404 response -> maps to DummyProductResult.NotFound, no retry
//     attempted.
//   - 500 response -> retried up to 3 attempts with exponential backoff,
//     then maps to DummyProductResult.ExternalServiceUnavailable if all
//     attempts fail. Assert the MockEngine saw exactly 3 requests.
//   - Use kotlinx-coroutines-test (runTest) to keep backoff delays fast in
//     the test rather than actually sleeping.
