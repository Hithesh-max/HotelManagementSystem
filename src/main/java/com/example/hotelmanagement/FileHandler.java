package com.example.hotelmanagement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * File persistence helper.
 *
 * Synchronization note:
 * These methods are static and may be called from different threads,
 * so we guard file read/write operations with a shared static lock.
 */
public class FileHandler {
    private static final String ROOM_FILE = "rooms.txt";
    private static final String BOOKING_FILE = "bookings.txt";

    /** Single lock for file operations */
    private static final Object FILE_LOCK = new Object();

    public static void saveRooms(ArrayList<Room> roomList) {
        synchronized (FILE_LOCK) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(ROOM_FILE))) {
                for (Room room : roomList) {
                    writer.write(room.toFileString());
                    writer.newLine();
                }
            } catch (IOException e) {
                System.out.println("Error saving room data: " + e.getMessage());
            }
        }
    }

    public static ArrayList<Room> loadRooms() {
        synchronized (FILE_LOCK) {
            ArrayList<Room> roomList = new ArrayList<>();
            File file = new File(ROOM_FILE);

            if (!file.exists()) {
                return roomList;
            }

            try {
                List<String> lines = Files.readAllLines(file.toPath());
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        Room room = Room.fromFileString(line);
                        if (room != null) {
                            roomList.add(room);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error loading room data: " + e.getMessage());
            }

            return roomList;
        }
    }

    public static void saveBookings(ArrayList<BookingRecord> bookingList) {
        synchronized (FILE_LOCK) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKING_FILE))) {
                for (BookingRecord record : bookingList) {
                    writer.write(record.toFileString());
                    writer.newLine();
                }
            } catch (IOException e) {
                System.out.println("Error saving booking data: " + e.getMessage());
            }
        }
    }

    public static ArrayList<BookingRecord> loadBookings() {
        synchronized (FILE_LOCK) {
            ArrayList<BookingRecord> bookingList = new ArrayList<>();
            File file = new File(BOOKING_FILE);

            if (!file.exists()) {
                return bookingList;
            }

            try {
                List<String> lines = Files.readAllLines(file.toPath());
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        BookingRecord record = BookingRecord.fromFileString(line);
                        if (record != null) {
                            bookingList.add(record);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error loading booking data: " + e.getMessage());
            }

            return bookingList;
        }
    }

    public static boolean exportText(String fileName, String content) {
        synchronized (FILE_LOCK) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
                writer.print(content);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

    public static boolean exportCsv(String fileName, List<BookingRecord> records) {
        synchronized (FILE_LOCK) {
            Path path = Paths.get(fileName);
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write("BookingId,Username,CustomerName,CustomerPhone,RoomNumber,RoomType,Days,RoomCharge,Tax,ServiceCharge,Total,Status,BookingDate,CheckInDate,CheckOutDate,PaymentMethod,PaymentStatus");
                writer.newLine();

                for (BookingRecord record : records) {
                    writer.write(csv(record.getBookingId()) + ","
                            + csv(record.getUsername()) + ","
                            + csv(record.getCustomerName()) + ","
                            + csv(record.getCustomerPhone()) + ","
                            + record.getRoomNumber() + ","
                            + csv(record.getRoomType()) + ","
                            + record.getDays() + ","
                            + record.getRoomCharge() + ","
                            + record.getTaxAmount() + ","
                            + record.getServiceCharge() + ","
                            + record.getTotalAmount() + ","
                            + csv(record.getStatus()) + ","
                            + csv(record.getBookingDate()) + ","
                            + csv(record.getCheckInDate()) + ","
                            + csv(record.getCheckOutDate()) + ","
                            + csv(record.getPaymentMethod()) + ","
                            + csv(record.getPaymentStatus()));
                    writer.newLine();
                }
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

    private static String csv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}