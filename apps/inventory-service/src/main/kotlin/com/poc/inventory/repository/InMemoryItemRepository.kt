package com.poc.inventory.repository

// TODO: implement an in-memory CRUD store for inventory items (a simple
// mutable map guarded appropriately for coroutine access is enough for this
// PoC — no real database). Should also back WatchStock (e.g. via a
// MutableSharedFlow of stock-change events) and ReserveItem's stock checks.
