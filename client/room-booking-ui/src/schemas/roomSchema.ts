import { z } from "zod";

export const roomSchema = z.object({
  roomName: z
    .string()
    .trim()
    .min(1, "Room name is required")
    .max(100, "Room name must be under 100 characters"),
  roomType: z.enum(["MEETING", "TRAINING", "POD"], {
    message: "Room type is required",
  }),
  capacity: z
    .number({ message: "Capacity is required" })
    .int("Capacity must be a whole number")
    .min(1, "Capacity must be at least 1"),
  location: z
    .string()
    .trim()
    .min(1, "Location is required")
    .max(100, "Location must be under 100 characters"),
});

export type RoomFormValues = z.infer<typeof roomSchema>;

export type RoomFormErrors = Partial<Record<keyof RoomFormValues, string>>;

export function validateRoomForm(values: unknown): {
  success: boolean;
  errors: RoomFormErrors;
} {
  const result = roomSchema.safeParse(values);
  if (result.success) {
    return { success: true, errors: {} };
  }
  const errors: RoomFormErrors = {};
  for (const issue of result.error.issues) {
    const field = issue.path[0] as keyof RoomFormValues;
    if (!errors[field]) {
      errors[field] = issue.message;
    }
  }
  return { success: false, errors };
}
