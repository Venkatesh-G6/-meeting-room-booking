import { UserCircle } from "lucide-react";

interface HeaderProps {
  title: string;
}

export default function Header({ title }: HeaderProps) {
  return (
    <header className="h-16 bg-white border-b border-gray-200 shadow-sm flex items-center justify-between px-6">
      <h1 className="text-lg font-semibold text-gray-800">{title}</h1>

      <div className="flex items-center gap-2 text-gray-600">
        <UserCircle className="w-6 h-6" />
        <span className="text-sm font-medium">Admin User</span>
      </div>
    </header>
  );
}
