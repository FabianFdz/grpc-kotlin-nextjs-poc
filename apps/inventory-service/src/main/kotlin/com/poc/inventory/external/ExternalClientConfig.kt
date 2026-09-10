package com.poc.inventory.external

// TODO: hold configuration for the DummyJSON HTTP client — base URL
// (configurable via env var, e.g. DUMMYJSON_BASE_URL, defaulting to
// https://dummyjson.com), request timeout, and retry parameters (max
// attempts, backoff base) used by DummyProductClient. Keep this as a plain
// data holder — no DI framework needed for a single implementation.
