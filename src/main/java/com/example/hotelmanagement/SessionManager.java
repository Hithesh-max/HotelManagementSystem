package com.example.hotelmanagement;

public class SessionManager {
    private static String currentUsername;
    private static String currentRole;

    public static void setSession(String username, String role) {
        currentUsername = username;
        currentRole = role;
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static String getCurrentRole() {
        return currentRole;
    }

    public static void clearSession() {
        currentUsername = null;
        currentRole = null;
    }

    public static boolean isStaff() {
        return "STAFF".equalsIgnoreCase(currentRole);
    }

    public static boolean isCustomer() {
        return "CUSTOMER".equalsIgnoreCase(currentRole);
    }
}