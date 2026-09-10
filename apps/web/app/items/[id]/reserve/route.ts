// TODO: call inventory-service's ReserveItem RPC via the server-side gRPC
// client. Must surface NOT_FOUND / FAILED_PRECONDITION gRPC errors from the
// RPC as appropriate HTTP status codes in the response.
export async function POST() {
  return new Response(null, { status: 501 });
}
