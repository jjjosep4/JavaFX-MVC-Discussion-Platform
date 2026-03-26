package guiAdminRequests;

import applicationMain.FoundationsMain;
import entityClasses.StaffRequest;
import entityClasses.User;
import guiAdminHome.ViewAdminHome;
import guiStaffRequests.ControllerStaffRequests;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class ViewAdminRequests {

    private static Stage theStage;
    private static User theUser;

    private static ListView<StaffRequest> list_Requests;
    private static TextArea text_Subject;
    private static TextArea text_Body;
    private static TextArea text_AdminReply;
    private static Label label_Status;

    private static StaffRequest selectedRequest = null;

    private static double width = FoundationsMain.WINDOW_WIDTH;
    private static double height = FoundationsMain.WINDOW_HEIGHT;

    public static void displayAdminRequests(Stage stage, User user) {
        theStage = stage;
        theUser = user;

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // ===================== LEFT SIDE =======================
        Label leftTitle = new Label("Staff Requests");
        leftTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        list_Requests = new ListView<>();
        
        // ADD CUSTOM CELL FACTORY TO DISPLAY REQUESTS PROPERLY
        list_Requests.setCellFactory(param -> new TextFieldListCell<>(new StringConverter<StaffRequest>() {
            @Override
            public String toString(StaffRequest r) {
                if (r == null) return "";
                String prefix;
                if (r.isClosed()) {
                    prefix = "[Closed]";
                } else if (r.getAdminReply() != null && !r.getAdminReply().trim().isEmpty()) {
                    prefix = "[Replied]";
                } else {
                    prefix = "[Open]";
                }
                return prefix + " " + r.getSubject() + " - from " + r.getSenderUserName();
            }

            @Override
            public StaffRequest fromString(String string) {
                return null;
            }
        }) {
            @Override
            public void updateItem(StaffRequest item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String prefix;
                    if (item.isClosed()) {
                        prefix = "[Closed]";
                    } else if (item.getAdminReply() != null && !item.getAdminReply().trim().isEmpty()) {
                        prefix = "[Replied]";
                    } else {
                        prefix = "[Open]";
                    }
                    setText(prefix + " " + item.getSubject() + " - from " + item.getSenderUserName());

                    // Bold unread items
                    if (!item.isRead()) {
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        list_Requests.setItems(ControllerAdminRequests.getAllRequests());

        list_Requests.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedRequest = newVal;
            populateRequestDetails(newVal);
        });

        VBox leftBox = new VBox(10, leftTitle, list_Requests);
        leftBox.setPrefWidth(350);
        root.setLeft(leftBox);

        // ===================== CENTER PANEL =======================
        Label lblSubject = new Label("Subject:");
        text_Subject = new TextArea();
        text_Subject.setEditable(false);
        text_Subject.setPrefHeight(60);

        Label lblBody = new Label("Message:");
        text_Body = new TextArea();
        text_Body.setEditable(false);
        text_Body.setPrefHeight(120);

        Label lblAdminReply = new Label("Admin Response:");
        text_AdminReply = new TextArea();
        text_AdminReply.setPrefHeight(120);

        label_Status = new Label("Status: ");

        Button btn_SaveResponse = new Button("Submit Request");
        btn_SaveResponse.setOnAction(e -> saveAdminResponse());

        Button btn_ToggleClosed = new Button("Open/Close Request");
        btn_ToggleClosed.setOnAction(e -> toggleRequestClosed());

        Button btn_ReturnHome = new Button("Return to Admin Home");
        btn_ReturnHome.setOnAction(e -> ControllerAdminRequests.returnToAdminHome(stage, user));

        VBox center = new VBox(
                10,
                lblSubject, text_Subject,
                lblBody, text_Body,
                lblAdminReply, text_AdminReply,
                label_Status,
                btn_SaveResponse, btn_ToggleClosed,
                btn_ReturnHome
        );

        center.setPadding(new Insets(10));
        root.setCenter(center);

        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.setTitle("Admin - Staff Requests");
        stage.show();
    }

    // ===================================================================
    // Populate request details WHEN SELECTED
    // ===================================================================
    private static void populateRequestDetails(StaffRequest r) {
        if (r == null) {
            text_Subject.clear();
            text_Body.clear();
            text_AdminReply.clear();
            label_Status.setText("Status:");
            return;
        }

        text_Subject.setText(r.getSubject());
        text_Body.setText(r.getBody());
        text_AdminReply.setText(r.getAdminReply() == null ? "" : r.getAdminReply());

        String status = r.isClosed() ? "Closed" : "Open";
        String readStatus = r.isRead() ? "Read" : "Unread";
        label_Status.setText("Status: " + status + " | " + readStatus + " | From: " + r.getSenderUserName());

        // Mark as read
        ControllerAdminRequests.markRead(r);

        refreshList();
    }

    // ===================================================================
    // Save admin response
    // ===================================================================
    private static void saveAdminResponse() {
        if (selectedRequest == null) return;

        String reply = text_AdminReply.getText().trim();

        StaffRequest updated = ControllerAdminRequests.updateAdminResponse(selectedRequest, reply);

        if (updated != null) {
            selectedRequest = updated;
            populateRequestDetails(updated);
        }

        refreshList();
    }

    // ===================================================================
    // Toggle Open / Closed
    // ===================================================================
    private static void toggleRequestClosed() {
        if (selectedRequest == null) return;

        StaffRequest updated = ControllerAdminRequests.toggleClosed(selectedRequest);

        if (updated != null) {
            selectedRequest = updated;
            populateRequestDetails(updated);
        }

        refreshList();
    }

    // ===================================================================
    // Refresh list panel
    // ===================================================================
    private static void refreshList() {
        list_Requests.setItems(ControllerAdminRequests.getAllRequests());
    }
}