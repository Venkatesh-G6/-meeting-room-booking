import type { ReactNode } from "react";
import Sidebar from "./Sidebar";
import Header from "./Header";

interface LayoutProps {
  title: string;
  children: ReactNode;
}

export default function Layout({ title, children }: LayoutProps) {
  return (
    <div className="flex min-h-screen">
      <Sidebar />

      <div className="flex-1 ml-64 flex flex-col">
        <Header title={title} />

        <main className="flex-1 overflow-y-auto bg-gray-50 p-6">
          {children}
        </main>
      </div>
    </div>
  );
}
