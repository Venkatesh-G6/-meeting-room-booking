type BadgeStatus =
  | "CONFIRMED"
  | "CANCELLED"
  | "AVAILABLE"
  | "NA";

interface BadgeProps {
  status: BadgeStatus;
}

const styles: Record<string, string> = {
  CONFIRMED: "bg-green-100 text-green-700",
  CANCELLED: "bg-red-100 text-red-700",
  AVAILABLE: "bg-green-100 text-green-700",
  NA: "bg-gray-100 text-gray-600",
};

export default function Badge({ status }: BadgeProps) {
  const key = String(status);
  const label = status;

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${styles[key]}`}
    >
      {label}
    </span>
  );
}
