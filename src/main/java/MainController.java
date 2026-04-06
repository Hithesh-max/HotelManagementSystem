


package com.example.hotelmanagement;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class MainController {

    @FXML private BorderPane mainRoot;
    @FXML private VBox sidebar;
    @FXML private StackPane contentArea;

    @FXML private Button dashboardBtn;
    @FXML private Button addRoomBtn;
    @FXML private Button viewRoomsBtn;
    @FXML private Button bookRoomBtn;
    @FXML private Button historyBtn;
    @FXML private Button cancelBtn;
    @FXML private Button extendBtn;
    @FXML private Button checkoutBtn;
    @FXML private Button deleteBtn;

    @FXML private Label welcomeLabel;
    @FXML private Label pageTitleLabel;
    @FXML private HBox toastBox;
    @FXML private Label toastLabel;
    @FXML private Label toastIconLabel;

    private final HotelManager hotelManager = new HotelManager();
    private TableView<Room> roomTable;
    private TableView<BookingRecord> historyTable;
    private final Queue<ToastMessage> toastQueue = new ArrayDeque<>();
    private boolean toastPlaying = false;

    @FXML

    public void initialize() {
        hotelManager.setRoomList(FileHandler.loadRooms());
        hotelManager.setBookingHistory(FileHandler.loadBookings());
        hotelManager.refreshRoomStatuses();

        setupRoomTable();
        setupHistoryTable();
        setupRoleUI();
        showDashboard();
    }


    private void showToast(String message) {
        toastLabel.setText(message);
        toastIconLabel.setText("⚠");

        toastBox.setVisible(true);
        toastBox.setManaged(true);
        toastBox.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), toastBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition pause = new PauseTransition(Duration.seconds(2));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toastBox);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        fadeOut.setOnFinished(e -> {
            toastBox.setVisible(false);
            toastBox.setManaged(false);
        });

        new SequentialTransition(fadeIn, pause, fadeOut).play();
    }





    private void setupRoleUI() {
        String username = SessionManager.getCurrentUsername();
        String role = SessionManager.getCurrentRole();

        if (username == null || role == null) {
            welcomeLabel.setText("Guest");
        } else {
            welcomeLabel.setText(username + " (" + role + ")");
        }

        if (SessionManager.isCustomer()) {
            addRoomBtn.setVisible(false);
            addRoomBtn.setManaged(false);

            checkoutBtn.setVisible(false);
            checkoutBtn.setManaged(false);

            deleteBtn.setVisible(false);
            deleteBtn.setManaged(false);
        }
    }

    private void hideButton(Button button) {
        button.setVisible(false);
        button.setManaged(false);
    }

    private void setupRoomTable() {
        roomTable = new TableView<>();

        TableColumn<Room, Integer> roomNoCol = new TableColumn<>("Room No");
        roomNoCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        TableColumn<Room, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("roomType"));

        TableColumn<Room, Integer> capacityCol = new TableColumn<>("Capacity");
        capacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));

        TableColumn<Room, Double> priceCol = new TableColumn<>("Price/Day");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("pricePerDay"));

        TableColumn<Room, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Room, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        roomTable.getColumns().addAll(roomNoCol, typeCol, capacityCol, priceCol, statusCol, customerCol);
        roomTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        roomTable.setPrefHeight(460);

        roomTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Room item, boolean empty) {
                super.updateItem(item, empty);

                getStyleClass().removeAll("room-row-booked", "room-row-available");

                if (empty || item == null) {
                    setStyle("");
                } else if (item.isBooked()) {
                    getStyleClass().add("room-row-booked");
                } else {
                    getStyleClass().add("room-row-available");
                }
            }
        });
    }

    private void setupHistoryTable() {
        historyTable = new TableView<>();
        historyTable.getStyleClass().add("premium-table");
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        historyTable.setPrefHeight(480);
        historyTable.setPlaceholder(new Label("No booking history found"));

        TableColumn<BookingRecord, String> idCol = new TableColumn<>("Booking ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("bookingId"));

        TableColumn<BookingRecord, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<BookingRecord, Integer> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        TableColumn<BookingRecord, String> dateCol = new TableColumn<>("Check-In");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));

        TableColumn<BookingRecord, String> outCol = new TableColumn<>("Check-Out");
        outCol.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));

        TableColumn<BookingRecord, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        totalCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : "₹" + String.format("%.2f", item));
            }
        });

        TableColumn<BookingRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        historyTable.getColumns().addAll(idCol, userCol, roomCol, dateCol, outCol, totalCol, statusCol);
        historyTable.setRowFactory(tv -> {
            TableRow<BookingRecord> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (!row.isEmpty()) {
                    showBookingDetailsDialog(row.getItem());
                }
            });
            return row;
        });
    }

    private void setActiveButton(Button active) {
        List<Button> buttons = List.of(
                dashboardBtn, addRoomBtn, viewRoomsBtn, bookRoomBtn,
                historyBtn, cancelBtn, extendBtn, checkoutBtn, deleteBtn
        );
        for (Button b : buttons) {
            b.getStyleClass().remove("side-button-active");
        }
        if (active != null && !active.getStyleClass().contains("side-button-active")) {
            active.getStyleClass().add("side-button-active");
        }
    }

    private void setPage(String title, Button activeButton, Node content) {
        pageTitleLabel.setText(title);
        setActiveButton(activeButton);
        setContent(content);
    }

    private void setContent(Node node) {
        contentArea.getChildren().setAll(node);
        StackPane.setAlignment(node, Pos.TOP_LEFT);
        FadeTransition fade = new FadeTransition(Duration.millis(220), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void enqueueToast(String type, String icon, String message) {
        toastQueue.offer(new ToastMessage(type, icon, message));
        if (!toastPlaying) {
            playNextToast();
        }
    }

    private void playNextToast() {
        ToastMessage toast = toastQueue.poll();
        if (toast == null) {
            toastPlaying = false;
            return;
        }

        toastPlaying = true;
        toastLabel.setText(toast.message());
        toastIconLabel.setText(toast.icon());
        toastBox.getStyleClass().removeAll("toast-success", "toast-warning", "toast-error");
        toastBox.getStyleClass().add(toast.type());
        toastBox.setVisible(true);
        toastBox.setManaged(true);
        toastBox.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(180), toastBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition pause = new PauseTransition(Duration.seconds(toast.type().equals("toast-error") ? 3.0 : 2.2));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(280), toastBox);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            toastBox.setVisible(false);
            toastBox.setManaged(false);
            playNextToast();
        });

        new SequentialTransition(fadeIn, pause, fadeOut).play();
    }

    public void showDashboard() {
        VBox root = pageContainer();

        Label subtitle = new Label(SessionManager.isStaff()
                ? "Monitor rooms, bookings, alerts and revenue from one premium dashboard."
                : "Track availability, stay details and your booking activity in one place.");
        subtitle.getStyleClass().add("subtle-text");

        HBox cards = new HBox(18);
        cards.getChildren().add(createStatCard(String.valueOf(hotelManager.getTotalRooms()), "Total Rooms"));
        cards.getChildren().add(createStatCard(String.valueOf(hotelManager.getAvailableRoomCount()), "Available"));
        cards.getChildren().add(createStatCard(String.valueOf(hotelManager.getOccupiedRoomCount()), "Occupied"));
        cards.getChildren().add(createStatCard(String.format("%.0f%%", hotelManager.getOccupancyPercent()), "Occupancy"));
        if (SessionManager.isStaff()) {
            cards.getChildren().add(createStatCard("₹" + formatMoney(hotelManager.getTodayRevenue()), "Today Revenue"));
            cards.getChildren().add(createStatCard(hotelManager.getTopRoomType(), "Top Room Type"));
        }

        HBox lower = new HBox(18);
        VBox leftPanel = createPanel("Live Overview", 580);
        leftPanel.getChildren().addAll(
                createInfoRow("Currently occupied rooms", String.valueOf(hotelManager.getOccupiedRoomCount())),
                createInfoRow("Available rooms", String.valueOf(hotelManager.getAvailableRoomCount())),
                createInfoRow("Occupancy badge", hotelManager.getOccupancyBadgeText()),
                createInfoRow("Low availability", hotelManager.isLowAvailability() ? "Warning" : "No"),
                buildQuickActionsBox()
        );

        VBox rightPanel = createPanel("Recent Bookings", 580);
        VBox bookingBox = new VBox(10);
        List<BookingRecord> recent = hotelManager.getRecentBookings(5);
        if (recent.isEmpty()) {
            bookingBox.getChildren().add(createEmptyLabel("No recent bookings yet."));
        } else {
            for (BookingRecord record : recent) {
                bookingBox.getChildren().add(createMiniBookingCard(record));
            }
        }
        rightPanel.getChildren().add(bookingBox);
        lower.getChildren().addAll(leftPanel, rightPanel);

        HBox bottom = new HBox(18);
        VBox occupiedPanel = createPanel("Currently Occupied Rooms", 580);
        List<Room> occupied = hotelManager.getOccupiedRooms();
        if (occupied.isEmpty()) {
            occupiedPanel.getChildren().add(createEmptyLabel("No occupied rooms currently."));
        } else {
            int shown = 0;
            for (Room room : occupied) {
                occupiedPanel.getChildren().add(createInfoRow(
                        "Room " + room.getRoomNumber() + " • " + room.getRoomType(),
                        room.getCustomerName().isBlank() ? room.getStatus() : room.getCustomerName()
                ));
                shown++;
                if (shown == 5) break;
            }
        }

        VBox alertPanel = createPanel("Availability Alert", 580);
        Label alert = new Label(hotelManager.isLowAvailability()
                ? "Only " + hotelManager.getAvailableRoomCount() + " rooms are available. Consider urgent room turnover."
                : "Availability is healthy. No urgent alert right now.");
        alert.getStyleClass().add(hotelManager.isLowAvailability() ? "warning-text" : "success-text");
        alert.setWrapText(true);
        alertPanel.getChildren().add(alert);
        bottom.getChildren().addAll(occupiedPanel, alertPanel);

        root.getChildren().addAll(subtitle, cards, lower, bottom);
        setPage("Dashboard", dashboardBtn, root);
    }

    public void showAddRoom() {
        if (!SessionManager.isStaff()) {
            showErrorToast("Only staff can add rooms.");
            return;
        }

        VBox form = createFormCard("Add Room", "Create a polished room entry with amenities, floor and status.");

        TextField roomNumberField = createField("Room Number");
        ComboBox<String> roomTypeCombo = combo("Standard", "Deluxe", "Suite", "AC", "Non-AC");
        TextField capacityField = createField("Capacity");
        TextField priceField = createField("Price Per Day");
        TextField floorField = createField("Floor Number");
        TextField imagePathField = createField("Image Placeholder / Path");
        TextField amenitiesField = createField("Amenities (comma separated)");
        ComboBox<String> statusCombo = combo(Room.STATUS_AVAILABLE, Room.STATUS_CLEANING, Room.STATUS_MAINTENANCE);
        statusCombo.setValue(Room.STATUS_AVAILABLE);

        Button saveBtn = primaryButton("Add Room");
        saveBtn.setOnAction(e -> {
            try {
                int roomNo = Integer.parseInt(roomNumberField.getText().trim());
                String type = roomTypeCombo.getValue();
                int cap = Integer.parseInt(capacityField.getText().trim());
                double price = Double.parseDouble(priceField.getText().trim());
                int floor = Integer.parseInt(floorField.getText().trim());

                if (type == null || type.isBlank()) {
                    showErrorToast("Select room type.");
                    return;
                }

                Room room = new Room(roomNo, type, cap, price, floor,
                        imagePathField.getText().trim(), amenitiesField.getText().trim(), statusCombo.getValue());

                if (!hotelManager.addRoom(room)) {
                    showErrorToast("Room number already exists.");
                    return;
                }

                FileHandler.saveRooms(hotelManager.getRoomList());
                showSuccessToast("Room added successfully");
                showViewRooms();
            } catch (Exception ex) {
                showErrorToast("Enter valid room details.");
            }
        });

        form.getChildren().addAll(roomNumberField, roomTypeCombo, capacityField, priceField, floorField,
                imagePathField, amenitiesField, statusCombo, saveBtn);
        setPage("Add Room", addRoomBtn, wrapCentered(form));
    }

    public void showViewRooms() {
        VBox root = pageContainer();

        Label subtitle = new Label("Search, sort, filter and manage premium rooms.");
        subtitle.getStyleClass().add("subtle-text");

        GridPane filters = new GridPane();
        filters.setHgap(12);
        filters.setVgap(12);
        filters.getStyleClass().add("filter-box");
        filters.setPadding(new Insets(18));

        TextField searchField = createField("Search room, type, amenities or status");
        ComboBox<String> sortCombo = combo("Room Number", "Price Low to High", "Price High to Low", "Capacity", "Room Type", "Floor");
        sortCombo.setValue("Room Number");

        CheckBox availableOnly = new CheckBox("Only Available");
        availableOnly.getStyleClass().add("check-box");

        TextField minPriceField = createField("Min Price");
        TextField maxPriceField = createField("Max Price");
        ComboBox<String> capacityCombo = combo("Any", "1", "2", "3", "4", "5");
        capacityCombo.setValue("Any");
        ComboBox<String> typeCombo = combo("Any", "Standard", "Deluxe", "Suite", "AC", "Non-AC");
        typeCombo.setValue("Any");
        ComboBox<String> statusCombo = combo("Any", Room.STATUS_AVAILABLE, Room.STATUS_RESERVED, Room.STATUS_OCCUPIED, Room.STATUS_CLEANING, Room.STATUS_MAINTENANCE);
        statusCombo.setValue("Any");
        ComboBox<String> floorCombo = combo("Any", "1", "2", "3", "4", "5");
        floorCombo.setValue("Any");

        Button applyBtn = primaryButton("Apply");
        Button resetBtn = secondaryButton("Reset");

        filters.add(searchField, 0, 0);
        filters.add(sortCombo, 1, 0);
        filters.add(availableOnly, 2, 0);
        filters.add(minPriceField, 0, 1);
        filters.add(maxPriceField, 1, 1);
        filters.add(capacityCombo, 2, 1);
        filters.add(typeCombo, 3, 1);
        filters.add(statusCombo, 4, 1);
        filters.add(floorCombo, 5, 1);
        filters.add(applyBtn, 6, 1);
        filters.add(resetBtn, 7, 1);

        searchField.setPrefWidth(260);
        sortCombo.setPrefWidth(200);

        Runnable applyLogic = () -> {
            try {
                ArrayList<Room> result = hotelManager.searchRooms(searchField.getText());
                Double minPrice = parseDoubleOrNull(minPriceField.getText());
                Double maxPrice = parseDoubleOrNull(maxPriceField.getText());
                Integer capacity = "Any".equals(capacityCombo.getValue()) ? null : Integer.parseInt(capacityCombo.getValue());
                String roomType = typeCombo.getValue();
                String status = statusCombo.getValue();
                Integer floorNumber = "Any".equals(floorCombo.getValue()) ? null : Integer.parseInt(floorCombo.getValue());

                result = hotelManager.filterRooms(result, availableOnly.isSelected(), minPrice, maxPrice,
                        capacity, roomType, status, floorNumber);
                result = hotelManager.sortRooms(result, sortCombo.getValue());
                roomTable.setItems(FXCollections.observableArrayList(result));
            } catch (Exception ex) {
                showErrorToast("Invalid filter values.");
            }
        };

        applyBtn.setOnAction(e -> {
            applyLogic.run();
            showSuccessToast("Filters applied");
        });

        resetBtn.setOnAction(e -> {
            searchField.clear();
            minPriceField.clear();
            maxPriceField.clear();
            availableOnly.setSelected(false);
            sortCombo.setValue("Room Number");
            capacityCombo.setValue("Any");
            typeCombo.setValue("Any");
            statusCombo.setValue("Any");
            floorCombo.setValue("Any");
            roomTable.setItems(FXCollections.observableArrayList(hotelManager.sortRooms(hotelManager.getRoomList(), "Room Number")));
            showWarningToast("Filters reset");
        });

        roomTable.setItems(FXCollections.observableArrayList(hotelManager.sortRooms(hotelManager.getRoomList(), "Room Number")));

        root.getChildren().addAll(subtitle, filters, roomTable);
        setPage("View Rooms", viewRoomsBtn, root);
    }

    public void showBookRoom() {
        VBox form = createFormCard("Book Room", "Use date-based booking with full bill breakdown.");

        TextField roomNoField = createField("Room Number");
        TextField nameField = createField("Customer Name");
        TextField phoneField = createField("Customer Phone");
        DatePicker checkInPicker = new DatePicker();
        DatePicker checkOutPicker = new DatePicker();
        checkInPicker.setPromptText("Check-In Date");
        checkOutPicker.setPromptText("Check-Out Date");
        ComboBox<String> paymentMethodCombo = combo("Cash", "Card", "UPI");
        paymentMethodCombo.setValue("UPI");

        if (SessionManager.isCustomer()) {
            nameField.setText(SessionManager.getCurrentUsername());
            nameField.setEditable(false);
        }

        Label summaryLabel = new Label("Choose room and dates to preview bill.");
        summaryLabel.getStyleClass().add("subtle-text");
        summaryLabel.setWrapText(true);

        Label suggestionLabel = new Label();
        suggestionLabel.getStyleClass().add("panel-text");
        suggestionLabel.setWrapText(true);

        HBox actions = new HBox(10);
        Button previewBtn = secondaryButton("Preview Summary");
        Button confirmBtn = primaryButton("Confirm Booking");
        actions.getChildren().addAll(previewBtn, confirmBtn);

        previewBtn.setOnAction(e -> {
            try {
                int roomNo = Integer.parseInt(roomNoField.getText().trim());
                LocalDate checkIn = checkInPicker.getValue();
                LocalDate checkOut = checkOutPicker.getValue();
                summaryLabel.setText(hotelManager.getPricePreviewByDates(roomNo, checkIn, checkOut));

                Room requested = hotelManager.findRoomByNumber(roomNo);
                if (requested == null) {
                    suggestionLabel.setText("Room not found.");
                    return;
                }
                if (!requested.canBook()) {
                    Room suggestion = hotelManager.getSuggestedRoom(roomNo);
                    if (suggestion != null) {
                        suggestionLabel.setText("Selected room unavailable. Suggested: Room "
                                + suggestion.getRoomNumber() + " | " + suggestion.getRoomType()
                                + " | Floor " + suggestion.getFloorNumber() + " | ₹"
                                + String.format("%.2f", suggestion.getPricePerDay()));
                    } else {
                        suggestionLabel.setText("No alternative room available.");
                    }
                } else {
                    suggestionLabel.setText("Room is available for booking.");
                }
            } catch (Exception ex) {
                summaryLabel.setText("Enter a valid room and dates.");
                suggestionLabel.setText("");
            }
        });

        confirmBtn.setOnAction(e -> {
            try {
                int roomNo = Integer.parseInt(roomNoField.getText().trim());
                String customerName = nameField.getText().trim();
                String customerPhone = phoneField.getText().trim();
                LocalDate checkIn = checkInPicker.getValue();
                LocalDate checkOut = checkOutPicker.getValue();
                String paymentMethod = paymentMethodCombo.getValue();

                if (customerName.isEmpty() || customerPhone.isEmpty()) {
                    showErrorToast("Name and phone are required.");
                    return;
                }
                if (!customerPhone.matches("[0-9]{10}")) {
                    showErrorToast("Phone number must contain exactly 10 digits.");
                    return;
                }
                if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
                    showErrorToast("Choose valid check-in and check-out dates.");
                    return;
                }

                String bookingUsername = SessionManager.isCustomer() ? SessionManager.getCurrentUsername() : customerName;
                BookingRecord record = hotelManager.bookRoom(roomNo, bookingUsername, customerName, customerPhone,
                        checkIn, checkOut, paymentMethod);
                if (record == null) {
                    showErrorToast("Room unavailable or booking details invalid.");
                    return;
                }

                FileHandler.saveRooms(hotelManager.getRoomList());
                FileHandler.saveBookings(hotelManager.getBookingHistory());
                showBookingConfirmationDialog(record);
                showSuccessToast("Room booked successfully");
                showDashboard();
            } catch (Exception ex) {
                showErrorToast("Enter valid booking details.");
            }
        });

        form.getChildren().addAll(roomNoField, nameField, phoneField, checkInPicker, checkOutPicker,
                paymentMethodCombo, actions, summaryLabel, suggestionLabel);
        setPage("Book Room", bookRoomBtn, wrapCentered(form));
    }

    public void showBookingHistory() {
        VBox root = pageContainer();
        Label subtitle = new Label("Search, filter, export and inspect every booking record.");
        subtitle.getStyleClass().add("subtle-text");

        GridPane filters = new GridPane();
        filters.setHgap(12);
        filters.setVgap(12);
        filters.getStyleClass().add("filter-box");
        filters.setPadding(new Insets(18));

        TextField queryField = createField("Search by booking ID / user / customer / room");
        ComboBox<String> statusCombo = combo("Any", "ACTIVE", "COMPLETED", "CANCELLED");
        statusCombo.setValue("Any");
        DatePicker fromPicker = new DatePicker();
        DatePicker toPicker = new DatePicker();
        Button applyBtn = primaryButton("Apply");
        Button exportTxtBtn = secondaryButton("Export TXT");
        Button exportCsvBtn = secondaryButton("Export CSV");

        filters.add(queryField, 0, 0);
        filters.add(statusCombo, 1, 0);
        filters.add(fromPicker, 2, 0);
        filters.add(toPicker, 3, 0);
        filters.add(applyBtn, 4, 0);
        filters.add(exportTxtBtn, 5, 0);
        filters.add(exportCsvBtn, 6, 0);

        ArrayList<BookingRecord> base = SessionManager.isCustomer()
                ? hotelManager.getHistoryForUser(SessionManager.getCurrentUsername())
                : hotelManager.getBookingHistory();

        ObservableList<BookingRecord> list = FXCollections.observableArrayList(base);
        historyTable.setItems(list);

        applyBtn.setOnAction(e -> {
            ArrayList<BookingRecord> filtered = hotelManager.filterHistory(base, statusCombo.getValue(),
                    queryField.getText(), fromPicker.getValue(), toPicker.getValue());
            historyTable.setItems(FXCollections.observableArrayList(filtered));
            showSuccessToast("History filters applied");
        });

        exportTxtBtn.setOnAction(e -> {
            List<BookingRecord> records = new ArrayList<>(historyTable.getItems());
            StringBuilder content = new StringBuilder();
            for (BookingRecord record : records) {
                content.append(hotelManager.generateInvoiceText(record, record.getPaymentMethod(), record.getPaymentStatus()))
                        .append("\n\n");
            }
            if (FileHandler.exportText("booking_history_export.txt", content.toString())) {
                showSuccessToast("History exported to booking_history_export.txt");
            } else {
                showErrorToast("Failed to export TXT.");
            }
        });

        exportCsvBtn.setOnAction(e -> {
            List<BookingRecord> records = new ArrayList<>(historyTable.getItems());
            if (FileHandler.exportCsv("booking_history_export.csv", records)) {
                showSuccessToast("History exported to booking_history_export.csv");
            } else {
                showErrorToast("Failed to export CSV.");
            }
        });

        root.getChildren().addAll(subtitle, filters, historyTable);
        setPage("Booking History", historyBtn, root);
    }

    public void showCancelBooking() {
        VBox form = createFormCard("Cancel Booking", "Customers can cancel their own active booking.");
        TextField roomNoField = createField("Room Number");
        Button cancelLocalBtn = primaryButton("Cancel Booking");

        cancelLocalBtn.setOnAction(e -> {
            try {
                int roomNo = Integer.parseInt(roomNoField.getText().trim());
                if (!SessionManager.isCustomer()) {
                    showErrorToast("Only customers can cancel their own bookings here.");
                    return;
                }

                String result = hotelManager.cancelBooking(SessionManager.getCurrentUsername(), roomNo);
                if (!result.startsWith("Booking cancelled")) {
                    showErrorToast(result);
                    return;
                }

                FileHandler.saveRooms(hotelManager.getRoomList());
                FileHandler.saveBookings(hotelManager.getBookingHistory());
                showWarningToast("Booking cancelled");
                showDashboard();
            } catch (Exception ex) {
                showErrorToast("Enter a valid room number.");
            }
        });

        form.getChildren().addAll(roomNoField, cancelLocalBtn);
        setPage("Cancel Booking", cancelBtn, wrapCentered(form));
    }

    public void showExtendStay() {
        VBox form = createFormCard("Extend Stay", "Select a new checkout date to extend the stay.");
        TextField roomNoField = createField("Room Number");
        DatePicker newCheckOutPicker = new DatePicker();
        Button extendLocalBtn = primaryButton("Extend Stay");

        extendLocalBtn.setOnAction(e -> {
            try {
                int roomNo = Integer.parseInt(roomNoField.getText().trim());
                LocalDate newCheckOut = newCheckOutPicker.getValue();
                if (!SessionManager.isCustomer()) {
                    showErrorToast("Only customers can extend their own stay here.");
                    return;
                }
                if (newCheckOut == null) {
                    showErrorToast("Select new checkout date.");
                    return;
                }

                String result = hotelManager.extendStay(SessionManager.getCurrentUsername(), roomNo, newCheckOut);
                if (!result.startsWith("Stay extended")) {
                    showErrorToast(result);
                    return;
                }

                FileHandler.saveRooms(hotelManager.getRoomList());
                FileHandler.saveBookings(hotelManager.getBookingHistory());
                showSuccessToast(result);
                showDashboard();
            } catch (Exception ex) {
                showErrorToast("Enter valid values.");
            }
        });

        form.getChildren().addAll(roomNoField, newCheckOutPicker, extendLocalBtn);
        setPage("Extend Stay", extendBtn, wrapCentered(form));
    }

    public void showCheckout() {
        if (!SessionManager.isStaff()) {
            showErrorToast("Only staff can checkout rooms.");
            return;
        }

        VBox form = createFormCard("Checkout", "Complete checkout with styled invoice and payment details.");
        TextField roomNoField = createField("Room Number");
        ComboBox<String> paymentMethodCombo = combo("Cash", "Card", "UPI");
        paymentMethodCombo.setValue("UPI");
        ComboBox<String> paymentStatusCombo = combo("Paid", "Pending");
        paymentStatusCombo.setValue("Paid");
        Button checkoutLocalBtn = primaryButton("Checkout Room");

        checkoutLocalBtn.setOnAction(e -> {
            try {
                int roomNo = Integer.parseInt(roomNoField.getText().trim());
                String invoice = hotelManager.checkoutRoom(roomNo, paymentMethodCombo.getValue(), paymentStatusCombo.getValue());
                if (!invoice.startsWith("HOTEL INVOICE")) {
                    showErrorToast(invoice);
                    return;
                }

                FileHandler.saveRooms(hotelManager.getRoomList());
                FileHandler.saveBookings(hotelManager.getBookingHistory());
                showInvoiceDialog(invoice);
                showSuccessToast("Checkout completed");
                showDashboard();
            } catch (Exception ex) {
                showErrorToast("Enter a valid room number.");
            }
        });

        form.getChildren().addAll(roomNoField, paymentMethodCombo, paymentStatusCombo, checkoutLocalBtn);
        setPage("Checkout", checkoutBtn, wrapCentered(form));
    }

    public void showDeleteRoom() {
        if (!SessionManager.isStaff()) {
            showErrorToast("Only staff can delete rooms.");
            return;
        }

        VBox form = createFormCard("Delete Room", "Remove a room that is not currently booked.");
        TextField roomNoField = createField("Room Number");
        Button deleteLocalBtn = primaryButton("Delete Room");

        deleteLocalBtn.setOnAction(e -> {
            try {
                int roomNo = Integer.parseInt(roomNoField.getText().trim());
                if (!hotelManager.deleteRoom(roomNo)) {
                    showErrorToast("Room not found or currently booked.");
                    return;
                }

                FileHandler.saveRooms(hotelManager.getRoomList());
                showWarningToast("Room deleted");
                showViewRooms();
            } catch (Exception ex) {
                showErrorToast("Enter a valid room number.");
            }
        });

        form.getChildren().addAll(roomNoField, deleteLocalBtn);
        setPage("Delete Room", deleteBtn, wrapCentered(form));
    }

    @FXML
    public void handleLogout() {
        try {
            SessionManager.clearSession();
            Stage stage = (Stage) contentArea.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 800);
            scene.getStylesheets().add(getClass().getResource("common.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("customer-theme.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Hotel Management System");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorToast("Failed to logout.");
        }
    }

    private VBox pageContainer() {
        VBox root = new VBox(18);
        root.setAlignment(Pos.TOP_LEFT);
        root.setFillWidth(true);
        return root;
    }

    private VBox createPanel(String title, double width) {
        VBox panel = new VBox(14);
        panel.getStyleClass().add("dashboard-section");
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(width);
        Label heading = new Label(title);
        heading.getStyleClass().add("section-heading");
        panel.getChildren().add(heading);
        return panel;
    }

    private Label createEmptyLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("subtle-text");
        return label;
    }

    private VBox createStatCard(String value, String labelText) {
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");

        Label label = new Label(labelText);
        label.getStyleClass().add("stat-label");

        VBox card = new VBox(8, valueLabel, label);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(180);
        return card;
    }

    private HBox createInfoRow(String labelText, String valueText) {
        Label label = new Label(labelText);
        label.getStyleClass().add("info-label");
        Label value = new Label(valueText);
        value.getStyleClass().add("info-value");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(10, label, spacer, value);
        row.getStyleClass().add("info-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox buildQuickActionsBox() {
        VBox wrap = new VBox(10);
        Label title = new Label("Quick Actions");
        title.getStyleClass().add("section-heading");

        HBox buttons = new HBox(10);
        Button book = primaryButton("Book Room");
        book.setOnAction(e -> showBookRoom());

        Button view = secondaryButton("View Rooms");
        view.setOnAction(e -> showViewRooms());

        Button checkout = secondaryButton("Checkout");
        checkout.setDisable(!SessionManager.isStaff());
        checkout.setOnAction(e -> showCheckout());

        buttons.getChildren().addAll(book, view, checkout);
        wrap.getChildren().addAll(title, buttons);
        return wrap;
    }

    private VBox createMiniBookingCard(BookingRecord record) {
        VBox box = new VBox(5);
        box.getStyleClass().add("mini-booking-card");
        Label line1 = new Label(record.getBookingId() + " • Room " + record.getRoomNumber() + " • " + record.getCustomerName());
        line1.getStyleClass().add("mini-booking-title");
        Label line2 = new Label(record.getCheckInDate() + " → " + record.getCheckOutDate() + " • " + record.getStatus());
        line2.getStyleClass().add("subtle-text");
        box.getChildren().addAll(line1, line2);
        return box;
    }

    private VBox createFormCard(String title, String subtitle) {
        VBox card = new VBox(14);
        card.getStyleClass().add("form-card");
        card.setPadding(new Insets(24));
        card.setMaxWidth(540);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("subtle-text");
        subtitleLabel.setWrapText(true);

        card.getChildren().addAll(titleLabel, subtitleLabel);
        return card;
    }

    private StackPane wrapCentered(Node node) {
        StackPane wrapper = new StackPane(node);
        wrapper.setPadding(new Insets(12));
        StackPane.setAlignment(node, Pos.TOP_CENTER);
        return wrapper;
    }

    private TextField createField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("primary-button");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("outline-button");
        return button;
    }

    private ComboBox<String> combo(String... items) {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(items);
        combo.setMaxWidth(Double.MAX_VALUE);
        return combo;
    }

    private void showEditRoomDialog(Room room) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Room");

        VBox root = new VBox(10);
        root.setPadding(new Insets(16));

        ComboBox<String> typeCombo = combo("Standard", "Deluxe", "Suite", "AC", "Non-AC");
        typeCombo.setValue(room.getRoomType());
        TextField capField = createField("Capacity");
        capField.setText(String.valueOf(room.getCapacity()));
        TextField priceField = createField("Price");
        priceField.setText(String.valueOf(room.getPricePerDay()));
        TextField floorField = createField("Floor Number");
        floorField.setText(String.valueOf(room.getFloorNumber()));
        TextField imageField = createField("Image Path");
        imageField.setText(room.getImagePath());
        TextField amenitiesField = createField("Amenities");
        amenitiesField.setText(room.getAmenities());
        ComboBox<String> statusCombo = combo(Room.STATUS_AVAILABLE, Room.STATUS_RESERVED, Room.STATUS_OCCUPIED, Room.STATUS_CLEANING, Room.STATUS_MAINTENANCE);
        statusCombo.setValue(room.getStatus());
        if (room.isBooked()) {
            statusCombo.setDisable(true);
        }

        root.getChildren().addAll(typeCombo, capField, priceField, floorField, imageField, amenitiesField, statusCombo);
        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    boolean updated = hotelManager.updateRoom(
                            room.getRoomNumber(),
                            typeCombo.getValue(),
                            Integer.parseInt(capField.getText().trim()),
                            Double.parseDouble(priceField.getText().trim()),
                            Integer.parseInt(floorField.getText().trim()),
                            imageField.getText().trim(),
                            amenitiesField.getText().trim(),
                            statusCombo.getValue()
                    );
                    if (updated) {
                        FileHandler.saveRooms(hotelManager.getRoomList());
                        showSuccessToast("Room updated successfully");
                        showViewRooms();
                    } else {
                        showErrorToast("Failed to update room.");
                    }
                } catch (Exception ex) {
                    showErrorToast("Invalid room edit data.");
                }
            }
        });
    }

    private void showRoomDetailsDialog(Room room) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Room Details");
        alert.setHeaderText("Room " + room.getRoomNumber() + " • " + room.getRoomType());
        alert.setContentText(
                "Floor: " + room.getFloorNumber() + "\n"
                        + "Capacity: " + room.getCapacity() + "\n"
                        + "Price: ₹" + formatMoney(room.getPricePerDay()) + "\n"
                        + "Status: " + room.getStatus() + "\n"
                        + "Amenities: " + room.getAmenities() + "\n"
                        + "Image Placeholder: " + (room.getImagePath().isBlank() ? "Not set" : room.getImagePath())
        );
        alert.showAndWait();
    }

    private void showBookingDetailsDialog(BookingRecord record) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Details");
        alert.setHeaderText("Booking ID: " + record.getBookingId());
        alert.setContentText(
                "Customer: " + record.getCustomerName() + "\n"
                        + "Room: " + record.getRoomNumber() + "\n"
                        + "Room Type: " + record.getRoomType() + "\n"
                        + "Check-In: " + record.getCheckInDate() + "\n"
                        + "Check-Out: " + record.getCheckOutDate() + "\n"
                        + "Room Charge: ₹" + formatMoney(record.getRoomCharge()) + "\n"
                        + "Tax: ₹" + formatMoney(record.getTaxAmount()) + "\n"
                        + "Service Charge: ₹" + formatMoney(record.getServiceCharge()) + "\n"
                        + "Total: ₹" + formatMoney(record.getTotalAmount()) + "\n"
                        + "Status: " + record.getStatus() + "\n"
                        + "Payment: " + record.getPaymentMethod() + " / " + record.getPaymentStatus()
        );
        alert.showAndWait();
    }

    private void showBookingConfirmationDialog(BookingRecord record) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Successful");
        alert.setHeaderText("Booking Confirmation");
        alert.setContentText(
                "Booking ID: " + record.getBookingId() + "\n"
                        + "Room: " + record.getRoomNumber() + "\n"
                        + "Customer: " + record.getCustomerName() + "\n"
                        + "Check-In: " + record.getCheckInDate() + "\n"
                        + "Check-Out: " + record.getCheckOutDate() + "\n"
                        + "Total: ₹" + formatMoney(record.getTotalAmount())
        );
        alert.showAndWait();
    }

    private void showInvoiceDialog(String invoice) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invoice");
        alert.setHeaderText("Checkout Completed");
        TextArea area = new TextArea(invoice);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefSize(520, 380);
        alert.getDialogPane().setContent(area);

        ButtonType exportButton = new ButtonType("Print Invoice", ButtonBar.ButtonData.LEFT);
        alert.getButtonTypes().add(exportButton);

        alert.showAndWait().ifPresent(result -> {
            if (result == exportButton) {
                String fileName = "invoice_" + System.currentTimeMillis() + ".txt";
                if (FileHandler.exportText(fileName, invoice)) {
                    showSuccessToast("Invoice exported to " + fileName);
                } else {
                    showErrorToast("Failed to export invoice.");
                }
            }
        });
    }

    private Double parseDoubleOrNull(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        return Double.parseDouble(text.trim());
    }

    private String formatMoney(double value) {
        return String.format("%.2f", value);
    }

    private void showSuccessToast(String message) {
        enqueueToast("toast-success", "✔", message);
    }

    private void showWarningToast(String message) {
        enqueueToast("toast-warning", "⚠", message);
    }

    private void showErrorToast(String message) {
        enqueueToast("toast-error", "✖", message);
    }

    private record ToastMessage(String type, String icon, String message) {}
}

