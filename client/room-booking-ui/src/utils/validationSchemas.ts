import { z } from "zod";

export const roomSchema = z.object({
  roomName: z.string()
    .min(2, "Room name must be at least 2 characters")
    .max(100, "Room name too long"),
  roomType: z.enum(["MEETING", "TRAINING", "POD"]),
  capacity: z.number()
    .min(1, "Capacity must be at least 1")
    .max(500, "Capacity too large"),
  location: z.string()
    .max(200, "Location too long")
    .optional()
});

export const bookingSchema = z.object({
  roomId: z.number().positive(),
  title: z.string()
    .min(2, "Title must be at least 2 characters")
    .max(200, "Title too long"),
  attendeeCount: z.number()
    .min(1, "At least 1 attendee"),
  bookedBy: z.string()
    .email("Valid email required"),
  startTime: z.string().min(1, "Start time required"),
  endTime: z.string().min(1, "End time required")
}).refine(
  data => data.endTime > data.startTime,
  {
    message: "End time must be after start time",
    path: ["endTime"]
  }
);

export const availabilitySchema = z.object({
  date: z.string().min(1, "Date is required"),
  startTime: z.string().min(1, "Start time required"),
  endTime: z.string().min(1, "End time required"),
  minCapacity: z.number().min(1, "At least 1")
}).refine(
  data => data.endTime > data.startTime,
  {
    message: "End time must be after start time",
    path: ["endTime"]
  }
);

export type RoomFormData = z.infer<typeof roomSchema>;
export type BookingFormData = z.infer<typeof bookingSchema>;
export type AvailabilityFormData = z.infer<typeof availabilitySchema>;
