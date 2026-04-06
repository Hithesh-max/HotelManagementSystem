package com.example.hotelmanagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Room {
    public static final String STATUS_AVAILABLE = "Available";
    public static final String STATUS_RESERVED = "Reserved";
    public static final String STATUS_OCCUPIED = "Occupied";
    public static final String STATUS_CLEANING = "Cleaning";
    public static final String STATUS_MAINTENANCE = "Maintenance";

    private int roomNumber;
    private String roomType;
    private int capacity;
    private double pricePerDay;
    private int floorNumber;
    private String imagePath;
    private String status;
    private String amenities;
    private String customerName;
    private String customerPhone;
    private int numberOfDays;

    public Room(int roomNumber, String roomType, int capacity, double pricePerDay,
                int floorNumber, String imagePath, String amenities, String status) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = capacity;
        this.pricePerDay = pricePerDay;
        this.floorNumber = floorNumber;
        this.imagePath = imagePath == null ? "" : imagePath;
        this.amenities = amenities == null ? "" : amenities;
        this.status = normalizeStatus(status);
        this.customerName = "";
        this.customerPhone = "";
        this.numberOfDays = 0;
    }

    public Room(int roomNumber, String roomType, int capacity, double pricePerDay) {
        this(roomNumber, roomType, capacity, pricePerDay, 1, "", "Wi-Fi, TV", STATUS_AVAILABLE);
    }

    private String normalizeStatus(String value) {
        if (value == null || value.isBlank()) {
            return STATUS_AVAILABLE;
        }

        if (value.equalsIgnoreCase(STATUS_AVAILABLE)) return STATUS_AVAILABLE;
        if (value.equalsIgnoreCase(STATUS_RESERVED)) return STATUS_RESERVED;
        if (value.equalsIgnoreCase(STATUS_OCCUPIED)) return STATUS_OCCUPIED;
        if (value.equalsIgnoreCase(STATUS_CLEANING)) return STATUS_CLEANING;
        if (value.equalsIgnoreCase(STATUS_MAINTENANCE)) return STATUS_MAINTENANCE;
        return STATUS_AVAILABLE;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getAmenities() {
        return amenities;
    }

    public String getStatus() {
        return status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public int getNumberOfDays() {
        return numberOfDays;
    }

    public boolean isBooked() {
        return STATUS_RESERVED.equals(status) || STATUS_OCCUPIED.equals(status);
    }

    public boolean canBook() {
        return STATUS_AVAILABLE.equals(status);
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setPricePerDay(double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath == null ? "" : imagePath;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities == null ? "" : amenities;
    }

    public void setStatus(String status) {
        this.status = normalizeStatus(status);
    }

    public List<String> getAmenitiesList() {
        List<String> list = new ArrayList<>();
        if (amenities == null || amenities.isBlank()) {
            return list;
        }
        for (String item : amenities.split(",")) {
            String value = item.trim();
            if (!value.isEmpty()) {
                list.add(value);
            }
        }
        return list;
    }

    public void setAmenitiesList(List<String> amenityList) {
        if (amenityList == null || amenityList.isEmpty()) {
            this.amenities = "";
            return;
        }
        this.amenities = String.join(", ", amenityList);
    }

    public void bookRoom(String customerName, String customerPhone, int numberOfDays) {
        this.customerName = customerName == null ? "" : customerName;
        this.customerPhone = customerPhone == null ? "" : customerPhone;
        this.numberOfDays = numberOfDays;
        this.status = STATUS_OCCUPIED;
    }

    public void reserveRoom(String customerName, String customerPhone, int numberOfDays) {
        this.customerName = customerName == null ? "" : customerName;
        this.customerPhone = customerPhone == null ? "" : customerPhone;
        this.numberOfDays = numberOfDays;
        this.status = STATUS_RESERVED;
    }

    public void extendStay(int extraDays) {
        this.numberOfDays += extraDays;
    }

    public double calculateBill() {
        return pricePerDay * numberOfDays;
    }

    public void checkoutRoom() {
        this.customerName = "";
        this.customerPhone = "";
        this.numberOfDays = 0;
        this.status = STATUS_CLEANING;
    }

    public void markAvailableAfterCleaning() {
        this.customerName = "";
        this.customerPhone = "";
        this.numberOfDays = 0;
        this.status = STATUS_AVAILABLE;
    }

    private static String esc(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("|", "\\p").replace(",", "\\c").replace("\n", " ");
    }

    private static String unesc(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\c", ",").replace("\\p", "|").replace("\\\\", "\\");
    }

    public String toFileString() {
        return roomNumber + "|"
                + esc(roomType) + "|"
                + capacity + "|"
                + pricePerDay + "|"
                + floorNumber + "|"
                + esc(imagePath) + "|"
                + esc(amenities) + "|"
                + esc(status) + "|"
                + esc(customerName) + "|"
                + esc(customerPhone) + "|"
                + numberOfDays;
    }

    public static Room fromFileString(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }

        if (line.contains("|")) {
            String[] data = line.split("\\|", -1);
            Room room = new Room(
                    Integer.parseInt(data[0]),
                    unesc(data[1]),
                    Integer.parseInt(data[2]),
                    Double.parseDouble(data[3]),
                    Integer.parseInt(data[4]),
                    unesc(data[5]),
                    unesc(data[6]),
                    unesc(data[7])
            );

            room.customerName = data.length > 8 ? unesc(data[8]) : "";
            room.customerPhone = data.length > 9 ? unesc(data[9]) : "";
            room.numberOfDays = data.length > 10 ? Integer.parseInt(data[10]) : 0;
            return room;
        }

        String[] data = line.split(",", -1);
        int roomNumber = Integer.parseInt(data[0]);
        String roomType = data[1];
        int capacity = Integer.parseInt(data[2]);
        double pricePerDay = Double.parseDouble(data[3]);
        boolean booked = Boolean.parseBoolean(data[4]);
        String customerName = data.length > 5 ? data[5] : "";
        String customerPhone = data.length > 6 ? data[6] : "";
        int numberOfDays = data.length > 7 ? Integer.parseInt(data[7]) : 0;

        Room room = new Room(roomNumber, roomType, capacity, pricePerDay, 1, "", "Wi-Fi, TV", STATUS_AVAILABLE);
        if (booked) {
            room.bookRoom(customerName, customerPhone, numberOfDays);
        }
        return room;
    }

    public static List<String> defaultStatuses() {
        return Arrays.asList(
                STATUS_AVAILABLE,
                STATUS_RESERVED,
                STATUS_OCCUPIED,
                STATUS_CLEANING,
                STATUS_MAINTENANCE
        );
    }
}