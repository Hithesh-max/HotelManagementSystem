package com.example.hotelmanagement;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserManager {

    private static final String FILE_NAME = "users.txt";
    private static final Map<String, String> passwordMap = new HashMap<>();
    private static final Map<String, String> roleMap = new HashMap<>();

    static {
        loadUsers();
        ensureDefaultStaffAccount();
    }

    public static boolean addCustomer(String username, String password) {
        if (username == null || password == null) {
            return false;
        }

        username = username.trim();
        password = password.trim();

        if (username.isEmpty() || password.isEmpty()) {
            return false;
        }

        if (passwordMap.containsKey(username)) {
            return false;
        }

        passwordMap.put(username, password);
        roleMap.put(username, "CUSTOMER");
        saveUsers();
        return true;
    }

    public static boolean validateUser(String username, String password, String role) {
        if (username == null || password == null || role == null) {
            return false;
        }

        username = username.trim();
        password = password.trim();
        role = role.trim().toUpperCase();

        return passwordMap.containsKey(username)
                && passwordMap.get(username).equals(password)
                && roleMap.containsKey(username)
                && roleMap.get(username).equalsIgnoreCase(role);
    }

    private static void ensureDefaultStaffAccount() {
        if (!passwordMap.containsKey("admin")) {
            passwordMap.put("admin", "admin123");
            roleMap.put("admin", "STAFF");
            saveUsers();
        }
    }

    private static void loadUsers() {
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);

                if (parts.length == 3) {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    String role = parts[2].trim().toUpperCase();

                    if (!username.isEmpty() && !password.isEmpty() && !role.isEmpty()) {
                        passwordMap.put(username, password);
                        roleMap.put(username, role);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    private static void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (String username : passwordMap.keySet()) {
                writer.write(username + "," + passwordMap.get(username) + "," + roleMap.get(username));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }
}


/*
package com.example.hotelmanagement;

import java.io.*;
import java.util.HashMap;

public class UserManager {

    private static final String FILE_NAME = "users.txt";
    private static HashMap<String, String> users = new HashMap<>();

    static {
        loadUsers();
    }

    public static boolean addUser(String username, String password) {
        if (users.containsKey(username)) {
            return false;
        }

        users.put(username, password);
        saveUsers();
        return true;
    }

    public static boolean validateUser(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(password);
    }

    private static void loadUsers() {
        File file = new File(FILE_NAME);

        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }

        } catch (IOException e) {
            System.out.println("Error loading users.");
        }
    }

    private static void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {

            for (String user : users.keySet()) {
                writer.write(user + "," + users.get(user));
                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error saving users.");
        }
    }
}
*/
