// TODO: item-with-pricing view. Server Component that calls
// inventory-service's GetItemWithPricing RPC, which itself calls
// pricing-service over internal gRPC. Render the item normally even if
// pricing data is absent (inventory-service degrades gracefully when
// pricing-service is unavailable) — show a "pricing unavailable" state
// rather than treating a missing price as an error.
export default function ItemWithPricingPage() {
  return null;
}
