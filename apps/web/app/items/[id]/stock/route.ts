// TODO: bridge inventory-service's server-streaming WatchStock RPC to the
// browser. Open a gRPC streaming call via @grpc/grpc-js (server-side only)
// and forward each update to the client as it arrives — e.g. as an
// text/event-stream (SSE) response using a ReadableStream. Must run on the
// Node.js runtime (not edge) since @grpc/grpc-js requires Node APIs.
export async function GET() {
  return new Response(null, { status: 501 });
}
