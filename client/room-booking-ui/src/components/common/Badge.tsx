type BadgeStatus =
  | "CONFIRMED"
  | "CANCELLED"
  | "MEETING"
  | "TRAINING"
  | "POD"
  | boolean;

interface BadgeProps {
  status: BadgeStatus;
}

const styles: Record<string, string> = {
  CONFIRMED: "bg-green-100 text-green-700",
  CANCELLED: "bg-red-100 text-red-700",
  MEETING: "bg-blue-100 text-blue-700",
  TRAINING: "bg-purple-100 text-purple-700",
  POD: "bg-yellow-100 text-yellow-700",
  true: "bg-green-100 text-green-700",
  false: "bg-gray-100 text-gray-600",
};

export default function Badge({ status }: BadgeProps) {
  const key = String(status);
  const label =
    typeof status === "boolean"
      ? status
        ? "Active"
        : "Inactive"
      : status;

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${styles[key]}`}
    >
      {label}
    </span>
  );
}
