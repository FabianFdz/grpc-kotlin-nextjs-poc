import type { ReactNode } from "react";

// TODO: replace with real root layout (nav, fonts, metadata) once the
// inventory UI takes shape. Kept minimal for the scaffold stage.
export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
