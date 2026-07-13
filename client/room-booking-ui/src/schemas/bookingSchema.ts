import { z } from "zod";

export const bookingSchema = z.object({
  roomId: z.string().min(1, "Room is required"),
  title: z
    .string()
    .trim()
    .min(1, "Meeting title is required")
    .max(150, "Title must be under 150 characters"),
  attendeeCount: z
    .number({ message: "Attendee count is required" })
    .int("Attendee count must be a whole number")
    .min(1, "At least 1 attendee is required"),
  startTime: z.string().min(1, "Start time is required"),
  endTime: z.string().min(1, "End time is required"),
});

export type BookingFormValues = z.infer<typeof bookingSchema>;

export type BookingFormErrors = Partial<Record<keyof BookingFormValues, string>>;

export function validateBookingForm(
  values: unknown,
  maxCapacity?: number
): { success: boolean; errors: BookingFormErrors } {
  const result = bookingSchema.safeParse(values);
  const errors: BookingFormErrors = {};

  if (!result.success) {
    for (const issue of result.error.issues) {
      const field = issue.path[0] as keyof BookingFormValues;
      if (!errors[field]) {
        errors[field] = issue.message;
      }
    }
  }

  if (
    maxCapacity !== undefined &&
    typeof (values as { attendeeCount?: number })?.attendeeCount === "number" &&
    (values as { attendeeCount: number }).attendeeCount > maxCapacity
  ) {
    errors.attendeeCount = `Attendee count exceeds room capacity (${maxCapacity})`;
  }

  return { success: Object.keys(errors).length === 0, errors };
}
