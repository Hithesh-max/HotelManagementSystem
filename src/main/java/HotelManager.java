package com.example.hotelmanagement;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HotelManager {
    public static final double TAX_RATE = 0.12;
    public static final double SERVICE_RATE = 0.05;

    private ArrayList<Room> roomList;
    private ArrayList<BookingRecord> bookingHistory;

    public HotelManager() {
        roomList = new ArrayList<>();
        bookingHistory = new ArrayList<>();
    }

    public void setRoomList(ArrayList<Room> roomList) {
        this.roomList = roomList;
    }

    public ArrayList<Room> getRoomList() {
        return roomList;
    }

    public void setBookingHistory(ArrayList<BookingRecord> bookingHistory) {
        this.bookingHistory = bookingHistory;
    }

    public ArrayList<BookingRecord> getBookingHistory() {
        return bookingHistory;
    }

    public boolean addRoom(Room room) {
        for (Room r : roomList) {
            if (r.getRoomNumber() == room.getRoomNumber()) {
                return false;
            }
        }
        roomList.add(room);
        return true;
    }

    public boolean updateRoom(int roomNumber, String roomType, int capacity, double pricePerDay,
                              int floorNumber, String imagePath, String amenities, String status) {
        Room room = findRoomByNumber(roomNumber);
        if (room == null) {
            return false;
        }

        room.setRoomType(roomType);
        room.setCapacity(capacity);
        room.setPricePerDay(pricePerDay);
        room.setFloorNumber(floorNumber);
        room.setImagePath(imagePath);
        room.setAmenities(amenities);

        if (!room.isBooked()) {
            room.setStatus(status);
        }
        return true;
    }


    private boolean datesOverlap(LocalDate newCheckIn, LocalDate newCheckOut,
                                 LocalDate existingCheckIn, LocalDate existingCheckOut) {
        return newCheckIn.isBefore(existingCheckOut) && newCheckOut.isAfter(existingCheckIn);
    }

    private boolean hasOverlappingActiveBooking(int roomNumber,
                                                LocalDate newCheckIn,
                                                LocalDate newCheckOut,
                                                String excludeBookingId) {
        for (BookingRecord record : bookingHistory) {
            if (record.getRoomNumber() != roomNumber) {
                continue;
            }

            if (!"ACTIVE".equalsIgnoreCase(record.getStatus())) {
                continue;
            }

            if (excludeBookingId != null && excludeBookingId.equals(record.getBookingId())) {
                continue;
            }

            LocalDate existingCheckIn = LocalDate.parse(record.getCheckInDate());
            LocalDate existingCheckOut = LocalDate.parse(record.getCheckOutDate());

            if (datesOverlap(newCheckIn, newCheckOut, existingCheckIn, existingCheckOut)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCurrentlyOccupied(BookingRecord record) {
        if (!"ACTIVE".equalsIgnoreCase(record.getStatus())) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalDate checkIn = LocalDate.parse(record.getCheckInDate());
        LocalDate checkOut = LocalDate.parse(record.getCheckOutDate());

        return (!today.isBefore(checkIn)) && today.isBefore(checkOut);
    }

    public void refreshRoomStatuses() {
        for (Room room : roomList) {
            BookingRecord currentBooking = null;

            for (BookingRecord record : bookingHistory) {
                if (record.getRoomNumber() == room.getRoomNumber() && isCurrentlyOccupied(record)) {
                    currentBooking = record;
                    break;
                }
            }

            if (currentBooking != null) {
                room.bookRoom(
                        currentBooking.getCustomerName(),
                        currentBooking.getCustomerPhone(),
                        currentBooking.getDays()
                );
            } else {
                if (Room.STATUS_OCCUPIED.equals(room.getStatus()) || Room.STATUS_RESERVED.equals(room.getStatus())) {
                    room.markAvailableAfterCleaning();
                }
            }
        }
    }





    public Room findRoomByNumber(int roomNumber) {
        for (Room room : roomList) {
            if (room.getRoomNumber() == roomNumber) {
                return room;
            }
        }
        return null;
    }

    public boolean deleteRoom(int roomNumber) {
        Room room = findRoomByNumber(roomNumber);
        if (room == null) {
            return false;
        }
        if (room.isBooked()) {
            return false;
        }
        return roomList.remove(room);
    }

    public ArrayList<Room> getAvailableRooms() {
        ArrayList<Room> result = new ArrayList<>();
        for (Room room : roomList) {
            if (Room.STATUS_AVAILABLE.equals(room.getStatus())) {
                result.add(room);
            }
        }
        return result;
    }

    public ArrayList<Room> getOccupiedRooms() {
        ArrayList<Room> result = new ArrayList<>();
        for (Room room : roomList) {
            if (Room.STATUS_OCCUPIED.equals(room.getStatus()) || Room.STATUS_RESERVED.equals(room.getStatus())) {
                result.add(room);
            }
        }
        return result;
    }

    public int getTotalRooms() {
        return roomList.size();
    }

    public int getAvailableRoomCount() {
        return getAvailableRooms().size();
    }

    public int getOccupiedRoomCount() {
        return getOccupiedRooms().size();
    }

    public ArrayList<Room> searchRooms(String query) {
        ArrayList<Room> result = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            result.addAll(roomList);
            return result;
        }

        String q = query.trim().toLowerCase();
        for (Room room : roomList) {
            if (String.valueOf(room.getRoomNumber()).contains(q)
                    || room.getRoomType().toLowerCase().contains(q)
                    || room.getStatus().toLowerCase().contains(q)
                    || String.valueOf(room.getFloorNumber()).contains(q)
                    || room.getAmenities().toLowerCase().contains(q)) {
                result.add(room);
            }
        }
        return result;
    }

    public ArrayList<Room> filterRooms(ArrayList<Room> source, boolean onlyAvailable,
                                       Double minPrice, Double maxPrice,
                                       Integer capacity, String roomType, String status, Integer floorNumber) {
        ArrayList<Room> result = new ArrayList<>();

        for (Room room : source) {
            if (onlyAvailable && !Room.STATUS_AVAILABLE.equals(room.getStatus())) {
                continue;
            }
            if (minPrice != null && room.getPricePerDay() < minPrice) {
                continue;
            }
            if (maxPrice != null && room.getPricePerDay() > maxPrice) {
                continue;
            }
            if (capacity != null && room.getCapacity() != capacity) {
                continue;
            }
            if (roomType != null && !roomType.isBlank() && !"Any".equalsIgnoreCase(roomType)
                    && !room.getRoomType().equalsIgnoreCase(roomType)) {
                continue;
            }
            if (status != null && !status.isBlank() && !"Any".equalsIgnoreCase(status)
                    && !room.getStatus().equalsIgnoreCase(status)) {
                continue;
            }
            if (floorNumber != null && room.getFloorNumber() != floorNumber) {
                continue;
            }
            result.add(room);
        }
        return result;
    }

    public ArrayList<Room> sortRooms(ArrayList<Room> source, String sortBy) {
        ArrayList<Room> sorted = new ArrayList<>(source);
        if (sortBy == null) {
            return sorted;
        }

        switch (sortBy) {
            case "Price Low to High" -> sorted.sort(Comparator.comparingDouble(Room::getPricePerDay));
            case "Price High to Low" -> sorted.sort(Comparator.comparingDouble(Room::getPricePerDay).reversed());
            case "Capacity" -> sorted.sort(Comparator.comparingInt(Room::getCapacity));
            case "Room Type" -> sorted.sort(Comparator.comparing(Room::getRoomType));
            case "Floor" -> sorted.sort(Comparator.comparingInt(Room::getFloorNumber));
            default -> sorted.sort(Comparator.comparingInt(Room::getRoomNumber));
        }
        return sorted;
    }

    public int calculateDays(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(checkIn, checkOut);
        return (int) Math.max(days, 0);
    }

    public double calculateRoomCharge(Room room, int days) {
        return room == null ? 0 : room.getPricePerDay() * days;
    }

    public double calculateTax(double roomCharge) {
        return roomCharge * TAX_RATE;
    }

    public double calculateServiceCharge(double roomCharge) {
        return roomCharge * SERVICE_RATE;
    }

    public double calculateTotal(double roomCharge) {
        return roomCharge + calculateTax(roomCharge) + calculateServiceCharge(roomCharge);
    }

    public String getPricePreviewByDates(int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        Room room = findRoomByNumber(roomNumber);
        if (room == null) {
            return "Room not found.";
        }

        int days = calculateDays(checkIn, checkOut);
        if (days <= 0) {
            return "Choose valid check-in and check-out dates.";
        }

        double roomCharge = calculateRoomCharge(room, days);
        double tax = calculateTax(roomCharge);
        double service = calculateServiceCharge(roomCharge);
        double total = roomCharge + tax + service;

        return "Room charge: ₹" + format(roomCharge)
                + " | Tax: ₹" + format(tax)
                + " | Service: ₹" + format(service)
                + " | Total: ₹" + format(total);
    }

    public Room getSuggestedRoom(int requestedRoomNumber) {
        Room requested = findRoomByNumber(requestedRoomNumber);
        ArrayList<Room> available = getAvailableRooms();
        if (available.isEmpty()) {
            return null;
        }

        Room best = null;
        for (Room room : available) {
            if (room.getRoomNumber() == requestedRoomNumber) {
                continue;
            }
            if (requested != null) {
                if (room.getCapacity() >= requested.getCapacity()) {
                    if (best == null || room.getPricePerDay() < best.getPricePerDay()) {
                        best = room;
                    }
                }
            } else {
                if (best == null || room.getPricePerDay() < best.getPricePerDay()) {
                    best = room;
                }
            }
        }
        return best == null ? available.get(0) : best;
    }




    public BookingRecord bookRoom(int roomNumber, String username, String customerName, String customerPhone,
                                  LocalDate checkIn, LocalDate checkOut, String paymentMethod) {

        Room room = findRoomByNumber(roomNumber);
        if (room == null) {
            return null;
        }

        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return null;
        }

        if (hasOverlappingActiveBooking(roomNumber, checkIn, checkOut, null)) {
            return null;
        }

        int days = calculateDays(checkIn, checkOut);
        if (days <= 0) {
            return null;
        }

        double roomCharge = calculateRoomCharge(room, days);
        double tax = calculateTax(roomCharge);
        double service = calculateServiceCharge(roomCharge);
        double total = roomCharge + tax + service;

        BookingRecord record = new BookingRecord(
                UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                username,
                customerName,
                customerPhone,
                room.getRoomNumber(),
                room.getRoomType(),
                room.getPricePerDay(),
                days,
                roomCharge,
                tax,
                service,
                total,
                "ACTIVE",
                LocalDate.now().toString(),
                "",
                checkIn.toString(),
                checkOut.toString(),
                paymentMethod,
                "Pending"
        );

        bookingHistory.add(record);
        refreshRoomStatuses();
        return record;
    }









    public ArrayList<BookingRecord> getHistoryForUser(String username) {
        ArrayList<BookingRecord> result = new ArrayList<>();
        for (BookingRecord record : bookingHistory) {
            if (record.getUsername().equalsIgnoreCase(username)) {
                result.add(record);
            }
        }
        return result;
    }

    public ArrayList<BookingRecord> filterHistory(ArrayList<BookingRecord> source,
                                                  String status,
                                                  String query,
                                                  LocalDate fromDate,
                                                  LocalDate toDate) {
        ArrayList<BookingRecord> result = new ArrayList<>();

        for (BookingRecord record : source) {
            if (status != null && !status.isBlank() && !"Any".equalsIgnoreCase(status)
                    && !record.getStatus().equalsIgnoreCase(status)) {
                continue;
            }

            if (query != null && !query.isBlank()) {
                String q = query.toLowerCase();
                boolean match = record.getBookingId().toLowerCase().contains(q)
                        || record.getCustomerName().toLowerCase().contains(q)
                        || record.getUsername().toLowerCase().contains(q)
                        || String.valueOf(record.getRoomNumber()).contains(q);
                if (!match) {
                    continue;
                }
            }

            LocalDate bookingDate = LocalDate.parse(record.getBookingDate());
            if (fromDate != null && bookingDate.isBefore(fromDate)) {
                continue;
            }
            if (toDate != null && bookingDate.isAfter(toDate)) {
                continue;
            }

            result.add(record);
        }

        result.sort(Comparator.comparing(BookingRecord::getBookingDate).reversed());
        return result;
    }

    public ArrayList<BookingRecord> getRecentBookings(int limit) {
        ArrayList<BookingRecord> copy = new ArrayList<>(bookingHistory);
        copy.sort(Comparator.comparing(BookingRecord::getBookingDate).reversed());
        if (copy.size() <= limit) {
            return copy;
        }
        return new ArrayList<>(copy.subList(0, limit));
    }

    public BookingRecord getActiveBookingForUserRoom(String username, int roomNumber) {
        for (BookingRecord record : bookingHistory) {
            if (record.getUsername().equalsIgnoreCase(username)
                    && record.getRoomNumber() == roomNumber
                    && record.getStatus().equalsIgnoreCase("ACTIVE")) {
                return record;
            }
        }
        return null;
    }

    public String cancelBooking(String username, int roomNumber) {
        BookingRecord record = getActiveBookingForUserRoom(username, roomNumber);
        if (record == null) {
            return "Active booking not found for this customer.";
        }

        Room room = findRoomByNumber(roomNumber);
        if (room != null) {
            room.markAvailableAfterCleaning();
        }

        record.setStatus("CANCELLED");
        record.setCloseDate(LocalDate.now().toString());
        record.setPaymentStatus("Cancelled");
        return "Booking cancelled successfully.";
    }

    public String extendStay(String username, int roomNumber, LocalDate newCheckOutDate) {
        BookingRecord record = getActiveBookingForUserRoom(username, roomNumber);
        if (record == null) {
            return "Active booking not found.";
        }

        LocalDate checkIn = LocalDate.parse(record.getCheckInDate());
        LocalDate currentCheckOut = LocalDate.parse(record.getCheckOutDate());

        if (newCheckOutDate == null || !newCheckOutDate.isAfter(currentCheckOut)) {
            return "New checkout must be after current checkout date.";
        }

        if (hasOverlappingActiveBooking(roomNumber, checkIn, newCheckOutDate, record.getBookingId())) {
            return "Cannot extend stay because this room is already booked for overlapping dates.";
        }

        Room room = findRoomByNumber(roomNumber);
        if (room == null) {
            return "Room not found.";
        }

        int newDays = calculateDays(checkIn, newCheckOutDate);
        record.setDays(newDays);
        refreshRoomStatuses();

        return "Stay extended successfully. New total days: " + newDays;
    }

    public BookingRecord getActiveBookingByRoom(int roomNumber) {
        for (BookingRecord record : bookingHistory) {
            if (record.getRoomNumber() == roomNumber && isCurrentlyOccupied(record)) {
                return record;
            }
        }
        return null;
    }

    public String checkoutRoom(int roomNumber, String paymentMethod, String paymentStatus) {
        Room room = findRoomByNumber(roomNumber);
        if (room == null) {
            return "Room not found.";
        }

        BookingRecord record = getActiveBookingByRoom(roomNumber);
        if (record == null) {
            return "Room is not currently booked.";
        }

        String invoice = generateInvoiceText(record, paymentMethod, paymentStatus);

        record.setStatus("COMPLETED");
        record.setCloseDate(LocalDate.now().toString());
        record.setPaymentMethod(paymentMethod);
        record.setPaymentStatus(paymentStatus);

        room.checkoutRoom();
        refreshRoomStatuses();

        return invoice;
    }

    public String generateInvoiceText(BookingRecord record, String paymentMethod, String paymentStatus) {
        return "HOTEL INVOICE\n"
                + "--------------------------------\n"
                + "Booking ID: " + record.getBookingId() + "\n"
                + "Customer: " + record.getCustomerName() + "\n"
                + "Phone: " + record.getCustomerPhone() + "\n"
                + "Room No: " + record.getRoomNumber() + "\n"
                + "Room Type: " + record.getRoomType() + "\n"
                + "Check-In: " + record.getCheckInDate() + "\n"
                + "Check-Out: " + record.getCheckOutDate() + "\n"
                + "Days: " + record.getDays() + "\n"
                + "Room Charge: ₹" + format(record.getRoomCharge()) + "\n"
                + "Tax: ₹" + format(record.getTaxAmount()) + "\n"
                + "Service Charge: ₹" + format(record.getServiceCharge()) + "\n"
                + "Total: ₹" + format(record.getTotalAmount()) + "\n"
                + "Payment Method: " + paymentMethod + "\n"
                + "Payment Status: " + paymentStatus + "\n"
                + "--------------------------------\n"
                + "Thank you for choosing Luxe Stay.";
    }

    public double getTotalRevenue() {
        double total = 0;
        for (BookingRecord record : bookingHistory) {
            if (record.getStatus().equalsIgnoreCase("COMPLETED")) {
                total += record.getTotalAmount();
            }
        }
        return total;
    }

    public double getTodayRevenue() {
        String today = LocalDate.now().toString();
        double total = 0;
        for (BookingRecord record : bookingHistory) {
            if (record.getStatus().equalsIgnoreCase("COMPLETED") && today.equals(record.getCloseDate())) {
                total += record.getTotalAmount();
            }
        }
        return total;
    }

    public double getOccupancyPercent() {
        if (roomList.isEmpty()) {
            return 0;
        }
        return (getOccupiedRoomCount() * 100.0) / roomList.size();
    }

    public boolean isLowAvailability() {
        return getAvailableRoomCount() <= 2 && getTotalRooms() > 0;
    }

    public String getTopRoomType() {
        if (bookingHistory.isEmpty()) {
            return "N/A";
        }

        Map<String, Integer> map = new HashMap<>();
        for (BookingRecord record : bookingHistory) {
            map.put(record.getRoomType(), map.getOrDefault(record.getRoomType(), 0) + 1);
        }

        String best = "N/A";
        int max = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                best = entry.getKey();
            }
        }
        return best;
    }

    public String getOccupancyBadgeText() {
        double percent = getOccupancyPercent();
        if (percent >= 85) return "High";
        if (percent >= 50) return "Moderate";
        return "Low";
    }

    private String format(double value) {
        return String.format("%.2f", value);
    }
}