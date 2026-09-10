// TODO: call inventory-service's EnrichItem RPC via the server-side gRPC
// client. This RPC internally calls the DummyJSON third-party API
// (asynchronously, with retry/timeout handling), so expect it to take
// longer than other item RPCs and handle that in the UI once implemented.
export async function POST() {
  return new Response(null, { status: 501 });
}
