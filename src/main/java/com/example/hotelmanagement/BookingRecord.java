package com.example.hotelmanagement;

public class BookingRecord {
    private String bookingId;
    private String username;
    private String customerName;
    private String customerPhone;
    private int roomNumber;
    private String roomType;
    private double pricePerDay;
    private int days;
    private double roomCharge;
    private double taxAmount;
    private double serviceCharge;
    private double totalAmount;
    private String status;
    private String bookingDate;
    private String closeDate;
    private String checkInDate;
    private String checkOutDate;
    private String paymentMethod;
    private String paymentStatus;

    public BookingRecord(String bookingId, String username, String customerName, String customerPhone,
                         int roomNumber, String roomType, double pricePerDay, int days,
                         double roomCharge, double taxAmount, double serviceCharge, double totalAmount,
                         String status, String bookingDate, String closeDate,
                         String checkInDate, String checkOutDate,
                         String paymentMethod, String paymentStatus) {
        this.bookingId = bookingId;
        this.username = username;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerDay = pricePerDay;
        this.days = days;
        this.roomCharge = roomCharge;
        this.taxAmount = taxAmount;
        this.serviceCharge = serviceCharge;
        this.totalAmount = totalAmount;
        this.status = status;
        this.bookingDate = bookingDate;
        this.closeDate = closeDate;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getUsername() {
        return username;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public int getDays() {
        return days;
    }

    public double getRoomCharge() {
        return roomCharge;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public double getServiceCharge() {
        return serviceCharge;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public String getCloseDate() {
        return closeDate;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setDays(int days) {
        this.days = days;
        this.roomCharge = this.pricePerDay * this.days;
        this.totalAmount = this.roomCharge + this.taxAmount + this.serviceCharge;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCloseDate(String closeDate) {
        this.closeDate = closeDate;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
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
        return esc(bookingId) + "|"
                + esc(username) + "|"
                + esc(customerName) + "|"
                + esc(customerPhone) + "|"
                + roomNumber + "|"
                + esc(roomType) + "|"
                + pricePerDay + "|"
                + days + "|"
                + roomCharge + "|"
                + taxAmount + "|"
                + serviceCharge + "|"
                + totalAmount + "|"
                + esc(status) + "|"
                + esc(bookingDate) + "|"
                + esc(closeDate) + "|"
                + esc(checkInDate) + "|"
                + esc(checkOutDate) + "|"
                + esc(paymentMethod) + "|"
                + esc(paymentStatus);
    }

    public static BookingRecord fromFileString(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }

        if (line.contains("|")) {
            String[] data = line.split("\\|", -1);
            return new BookingRecord(
                    unesc(data[0]),
                    unesc(data[1]),
                    unesc(data[2]),
                    unesc(data[3]),
                    Integer.parseInt(data[4]),
                    unesc(data[5]),
                    Double.parseDouble(data[6]),
                    Integer.parseInt(data[7]),
                    Double.parseDouble(data[8]),
                    Double.parseDouble(data[9]),
                    Double.parseDouble(data[10]),
                    Double.parseDouble(data[11]),
                    unesc(data[12]),
                    unesc(data[13]),
                    unesc(data[14]),
                    data.length > 15 ? unesc(data[15]) : "",
                    data.length > 16 ? unesc(data[16]) : "",
                    data.length > 17 ? unesc(data[17]) : "",
                    data.length > 18 ? unesc(data[18]) : "Pending"
            );
        }

        String[] data = line.split(",", -1);
        double pricePerDay = Double.parseDouble(data[6]);
        int days = Integer.parseInt(data[7]);
        double totalAmount = Double.parseDouble(data[8]);
        double roomCharge = pricePerDay * days;
        double extra = totalAmount - roomCharge;
        double tax = extra > 0 ? extra * 0.7 : 0;
        double service = extra > 0 ? extra * 0.3 : 0;

        return new BookingRecord(
                data[0],
                data[1],
                data[2],
                data[3],
                Integer.parseInt(data[4]),
                data[5],
                pricePerDay,
                days,
                roomCharge,
                tax,
                service,
                totalAmount,
                data[9],
                data[10],
                data[11],
                data[10],
                data[11].isBlank() ? data[10] : data[11],
                "",
                "Pending"
        );
    }
}