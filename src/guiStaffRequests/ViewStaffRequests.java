package guiStaffRequests;

import applicationMain.FoundationsMain;

import entityClasses.User;
import entityClasses.StaffRequest;
import guiUserUpdate.ViewUserUpdate;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

/*******
 * <p> Title: ViewStaffRequests Class. </p>
 * 
 * <p> Description: JavaFX view for the Staff ↔ Admin Requests screen. The
 * left side shows the staff member's requests (with visual cues for open,
 * closed, updated, and unread); the right side shows details and editing
 * controls for the selected request. </p>
 *
 * <p> Structure and interfaces: </p>
 * <ul>
 *   <li>{@link #displayStaffRequests(Stage, User)} is the public entry point
 *       used by staff navigation code.</li>
 *   <li>Uses {@link ControllerStaffRequests} for all data access and state
 *       changes; this class contains no direct database calls.</li>
 *   <li>Uses a lazily constructed singleton-style view so the layout is
 *       created once and reused when staff navigate away and back. </li>
 *   <li>The {@link ListView} binds to an observable list returned by
 *       {@link ControllerStaffRequests#loadRequestsForUser(User)} and uses
 *       a custom cell factory to show status prefixes and bold unread items.</li>
 * </ul>
 *
 * <p> Data used and produced: </p>
 * <ul>
 *   <li>Uses the current {@link User} to filter requests and populate the
 *       “User” label.</li>
 *   <li>Displays {@link StaffRequest} data (subject, body, admin reply,
 *       status, read flag) for the selection.</li>
 *   <li>Produces create/update/mark-read requests that the controller
 *       persists to the database, then reloads the updated objects. </li>
 * </ul>
 *
 * <p> Layout notes: The window width is slightly wider than the standard
 * home pages so that the list and detail panels can be shown side-by-side
 * without crowding, but it still follows the general FoundationsF25
 * visual style (top banner + separator + main content). </p>
 *
 * <p> Validation and testing: The behavior of this view is validated by
 * GUI and integration tests such as {@code ViewStaffRequestsTest}, which
 * verify that:
 * </p>
 * <ul>
 *   <li>the list reflects the current staff user’s requests,</li>
 *   <li>new and edited requests are persisted and re-selected,</li>
 *   <li>unread items are rendered in bold and updated when "Mark as Read"
 *       is used, and</li>
 *   <li>the filter for closed requests behaves as expected.</li>
 * </ul>
 */
public class ViewStaffRequests {

    /*-----------------------------
     * Attributes
     *---------------------------*/

    // Slightly wider than the standard main window to comfortably show
    // the list and detail panel side-by-side while keeping text readable.
    private static double width  = 950;
    private static double height = 600;

    // GUI Area 1: title, user details, account update
    protected static Label  label_PageTitle   = new Label();
    protected static Label  label_UserDetails = new Label();
    protected static Button button_UpdateThisUser = new Button("Account Update");

    // Separator between header and main content
    protected static Line line_Separator1 = new Line(20, 95, width - 20, 95);

    // Left panel: requests list
    protected static Label    label_Requests = new Label("Staff ↔ Admin Requests");
    protected static ListView<StaffRequest> list_Requests =
            new ListView<>();

    // Right panel: details
    protected static Label     label_Subject      = new Label("Subject:");
    protected static TextField text_Subject      = new TextField();

    protected static Label     label_Body        = new Label("Request Body:");
    protected static TextArea  text_Body         = new TextArea();

    protected static Label     label_AdminReply  = new Label("Admin Response (read-only):");
    protected static TextArea  text_AdminReply   = new TextArea();

    protected static Label     label_Status      = new Label();
    protected static Label     label_ReadStatus  = new Label();

    protected static Button    button_New        = new Button("Clear for New Request");
    protected static Button    button_Save       = new Button("Submit Request");
    protected static Button    button_ToggleClosed = new Button("Toggle Closed");
    protected static Button    button_MarkRead   = new Button("Mark as Read");
    protected static Button    button_Return     = new Button("Return to Staff Home");

    // Separator before bottom
    protected static Line line_Separator2 = new Line(20, 750, width - 20, 750);

    protected static Stage theStage;
    protected static Pane  theRootPane;
    protected static User  theUser;

    private static Scene theScene;
    private static ViewStaffRequests theView;

    // List filter flag; see handleToggleClosed() for behavior.
    private static boolean showClosed = true;

    /*-----------------------------
     * Entry point
     *---------------------------*/

    /*******
     * <p> Method: displayStaffRequests </p>
     * 
     * <p> Description: Configure and display the Staff Requests page for the
     * given user and stage. This method sets shared references, builds the
     * GUI on first use, then populates user data and request list on each
     * invocation. </p>
     *
     * <p> Why this approach: The singleton-style view is consistent with
     * other GUI classes in the FoundationsF25 project and avoids recreating
     * the entire layout each time staff navigate to this screen. </p>
     * 
     * @param stage the {@link Stage} used by the JavaFX application
     * @param user  the currently logged-in staff {@link User}
     */
    public static void displayStaffRequests(Stage stage, User user) {
        theStage = stage;
        theUser  = user;

        if (theView == null) {
            theView = new ViewStaffRequests();
            theRootPane = new Pane();
            theScene = new Scene(theRootPane, width, height);
            setupGUI();
        }

        populateUserData();
        refreshRequests();

        theStage.setScene(theScene);
        theStage.setTitle("Staff ↔ Admin Requests");
        theStage.show();
    }

    /*-----------------------------
     * GUI setup
     *---------------------------*/

    /** Private constructor to enforce the singleton-style usage of this view. */
    private ViewStaffRequests() {
        // singleton constructor
    }

    /*******
     * <p> Method: setupGUI </p>
     * 
     * <p> Description: Configure all GUI widgets (labels, buttons, list,
     * text fields) including fonts, positions, and event handlers. Invoked
     * only once when the view is first created. </p>
     *
     * <p> The method follows the common pattern used in other views:
     * top banner (title + user details), separator, then main content split
     * into left and right areas with bottom navigation controls. </p>
     */
    private static void setupGUI() {

        // GUI Area 1
        label_PageTitle.setText("Staff ↔ Admin Requests");
        setupLabel(label_PageTitle, "Arial", 24, width, Pos.CENTER, 0, 10);

        setupLabel(label_UserDetails, "Arial", 16, width, Pos.BASELINE_LEFT, 20, 55);

        setupButton(button_UpdateThisUser, "Dialog", 14, 170, Pos.CENTER, 600, 50);
        button_UpdateThisUser.setOnAction(e ->
                ViewUserUpdate.displayUserUpdate(theStage, theUser));

        line_Separator1.setStartX(20);
        line_Separator1.setStartY(95);
        line_Separator1.setEndX(width - 20);
        line_Separator1.setEndY(95);

        // Left panel: list of requests
        setupLabel(label_Requests, "Arial", 16, 250, Pos.BASELINE_LEFT, 20, 110);

        list_Requests.setLayoutX(20);
        list_Requests.setLayoutY(140);
        list_Requests.setPrefWidth(300);
        list_Requests.setPrefHeight(350); // visually about 5–8 rows, scrollable

        // Custom cell factory: prefix status and bold unread items so staff
        // can quickly scan for new or updated requests without reading the
        // full details pane.
        list_Requests.setCellFactory(param -> new TextFieldListCell<>(new StringConverter<>() {
            @Override
            public String toString(StaffRequest r) {
                if (r == null) return "";
                String prefix;
                if (r.isClosed()) {
                    prefix = "[Closed]";
                } else if (r.getAdminReply() != null && !r.getAdminReply().trim().isEmpty()) {
                    // Treat any admin response on an open request as "Updated"
                    prefix = "[Updated]";
                } else {
                    prefix = "[Open]";
                }
                return prefix + " " + r.getSubject();
            }

            @Override
            public StaffRequest fromString(String string) {
                return null; // we don't do inline editing from the list cells
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
                        prefix = "[Updated]";
                    } else {
                        prefix = "[Open]";
                    }
                    setText(prefix + " " + item.getSubject());

                    // Unread requests are rendered in bold to draw attention.
                    if (!item.isRead()) {
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        list_Requests.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSel, newSel) -> showRequestDetails(newSel));

        // Right panel: details
        setupLabel(label_Subject, "Arial", 14, 200, Pos.BASELINE_LEFT, 340, 110);
        text_Subject.setLayoutX(340);
        text_Subject.setLayoutY(130);
        text_Subject.setPrefWidth(380);

        setupLabel(label_Body, "Arial", 14, 200, Pos.BASELINE_LEFT, 340, 165);
        text_Body.setLayoutX(340);
        text_Body.setLayoutY(185);
        text_Body.setPrefWidth(380);
        text_Body.setPrefHeight(120);
        text_Body.setWrapText(true);

        setupLabel(label_AdminReply, "Arial", 14, 250, Pos.BASELINE_LEFT, 340, 315);
        text_AdminReply.setLayoutX(340);
        text_AdminReply.setLayoutY(335);
        text_AdminReply.setPrefWidth(380);
        text_AdminReply.setPrefHeight(120);
        text_AdminReply.setWrapText(true);
        text_AdminReply.setEditable(false); // staff cannot edit admin reply

        setupLabel(label_Status, "Arial", 13, 200, Pos.BASELINE_LEFT, 340, 465);
        setupLabel(label_ReadStatus, "Arial", 13, 200, Pos.BASELINE_LEFT, 540, 465);

        // Buttons row
        setupButton(button_New, "Dialog", 13, 160, Pos.CENTER, 300, 500);
        button_New.setOnAction(e -> handleNewRequest());

        setupButton(button_Save, "Dialog", 13, 130, Pos.CENTER, 480, 500);
        button_Save.setOnAction(e -> handleSaveRequest());

        setupButton(button_ToggleClosed, "Dialog", 13, 130, Pos.CENTER, 620, 500);
        button_ToggleClosed.setOnAction(e -> handleToggleClosed());

        setupButton(button_MarkRead, "Dialog", 13, 130, Pos.CENTER, 760, 500);
        button_MarkRead.setOnAction(e -> handleMarkRead());

        // Separator + bottom row
        line_Separator2.setStartX(20);
        line_Separator2.setStartY(535);
        line_Separator2.setEndX(width - 20);
        line_Separator2.setEndY(535);

        setupButton(button_Return, "Dialog", 14, 200, Pos.CENTER, 20, 550);
        button_Return.setOnAction(e -> 
                ControllerStaffRequests.returnToStaffHome(theStage, theUser));

        // Add everything to root pane
        theRootPane.getChildren().addAll(
                label_PageTitle,
                label_UserDetails,
                button_UpdateThisUser,
                line_Separator1,
                label_Requests,
                list_Requests,
                label_Subject,
                text_Subject,
                label_Body,
                text_Body,
                label_AdminReply,
                text_AdminReply,
                label_Status,
                label_ReadStatus,
                button_New,
                button_Save,
                button_ToggleClosed,
                button_MarkRead,
                line_Separator2,
                button_Return
        );
    }

    /*-----------------------------
     * Helper UI setup methods
     *---------------------------*/

    /*******
     * <p> Method: setupLabel </p>
     *
     * <p> Description: Utility method to configure font, preferred width,
     * alignment, and position for {@link Label} controls. Centralizing this
     * code helps keep labels visually consistent across the screen. </p>
     */
    private static void setupLabel(Label label, String font, int size,
                                   double width, Pos align, double x, double y) {
        label.setFont(new Font(font, size));
        label.setPrefWidth(width);
        label.setAlignment(align);
        label.setLayoutX(x);
        label.setLayoutY(y);
    }

    /*******
     * <p> Method: setupButton </p>
     *
     * <p> Description: Utility method to configure font, preferred width,
     * alignment, and position for {@link Button} controls so that all buttons
     * share a common look-and-feel. </p>
     */
    private static void setupButton(Button button, String font, int size,
                                    double width, Pos align, double x, double y) {
        button.setFont(new Font(font, size));
        button.setPrefWidth(width);
        button.setAlignment(align);
        button.setLayoutX(x);
        button.setLayoutY(y);
    }

    /*-----------------------------
     * Data / event helpers
     *---------------------------*/

    /*******
     * <p> Method: populateUserData </p>
     *
     * <p> Description: Populate the top-left user details label with the
     * current user's username. If no user is set, shows a placeholder. </p>
     */
    private static void populateUserData() {
        if (theUser != null) {
            label_UserDetails.setText("User: " + theUser.getUserName());
        } else {
            label_UserDetails.setText("User: (none)");
        }
    }

    /*******
     * <p> Method: refreshRequests </p>
     *
     * <p> Description: Reload the list of requests for the current staff user
     * from the controller. This is called when the screen is shown and after
     * create/update/mark-read operations so that the list reflects the
     * current database state. </p>
     */
    private static void refreshRequests() {
        list_Requests.setItems(
            ControllerStaffRequests.loadRequestsForUser(theUser)
        );
    }

    /*******
     * <p> Method: showRequestDetails </p>
     *
     * <p> Description: Display the subject, body, admin reply, and status
     * fields for the given {@link StaffRequest}. Passing {@code null} clears
     * the fields and displays placeholder status text. </p>
     *
     * @param r the selected {@link StaffRequest}, or {@code null} to clear
     */
    private static void showRequestDetails(StaffRequest r) {
        if (r == null) {
            text_Subject.clear();
            text_Body.clear();
            text_AdminReply.clear();
            label_Status.setText("Status: (none)");
            label_ReadStatus.setText("Read: (n/a)");
            return;
        }

        text_Subject.setText(r.getSubject());
        text_Body.setText(r.getBody());
        text_AdminReply.setText(r.getAdminReply());
        label_Status.setText("Status: " + (r.isClosed() ? "Closed" : "Open"));
        label_ReadStatus.setText("Read: " + (r.isRead() ? "Yes" : "No"));
    }

    /*******
     * <p> Method: handleNewRequest </p>
     *
     * <p> Description: Clear the input fields and reset the status labels
     * so the user can enter a brand new request. This does not write
     * anything to the database until {@link #handleSaveRequest()} is called. </p>
     */
    private static void handleNewRequest() {
        text_Subject.clear();
        text_Body.clear();
        text_AdminReply.clear();
        label_Status.setText("Status: New (not yet saved)");
        label_ReadStatus.setText("Read: No");
    }

    /*******
     * <p> Method: handleSaveRequest </p>
     *
     * <p> Description: Save button handler. If no request is selected, this
     * method attempts to create a new request via
     * {@link ControllerStaffRequests#createNewRequest(User, String, String)}.
     * If a request is selected, it attempts to update that request’s subject
     * and body via
     * {@link ControllerStaffRequests#updateRequestBody(StaffRequest, String, String)}. </p>
     *
     * <p> Why this approach: Reusing a single button for both “new” and
     * “update” keeps the UI simple. The selected-item check makes it clear
     * whether we are creating or editing. The controller is responsible for
     * ignoring edits on closed requests and for validating blank fields. </p>
     */
    private static void handleSaveRequest() {
        String subject = text_Subject.getText();
        String body    = text_Body.getText();

        StaffRequest selected =
                list_Requests.getSelectionModel().getSelectedItem();

        if (selected == null) {
            // Treat as new request
            StaffRequest created =
                    ControllerStaffRequests.createNewRequest(theUser, subject, body);
            if (created != null) {
                refreshRequests();
                list_Requests.getSelectionModel().select(created);
                showRequestDetails(created);
            }
        } else {
            // Update existing open request (controller enforces "open only")
            StaffRequest updated =
                    ControllerStaffRequests.updateRequestBody(selected, subject, body);
            refreshRequests();
            list_Requests.getSelectionModel().select(updated);
            showRequestDetails(updated);
        }
    }

    /*******
     * <p> Method: handleToggleClosed </p>
     *
     * <p> Description: Toggle the list view filter between showing all
     * requests and showing only open requests. This method does <b>not</b>
     * change any data in the database; it only changes which items are
     * displayed and updates the button label accordingly. </p>
     *
     * <p> Why this approach: The requirements call for staff to be able to
     * hide closed items in the list without actually re-opening or closing
     * requests. A local filter flag ({@link #showClosed}) keeps the UI
     * behavior independent from the request’s persisted closed state. </p>
     */
    private static void handleToggleClosed() {
        showClosed = !showClosed;

        if (showClosed) {
            // show ALL requests again
            list_Requests.setItems(
                    ControllerStaffRequests.loadRequestsForUser(theUser)
            );
            button_ToggleClosed.setText("Hide Closed");
        } else {
            // filter out CLOSED items
            var all = ControllerStaffRequests.loadRequestsForUser(theUser);
            var filtered = all.filtered(r -> !r.isClosed());
            list_Requests.setItems(filtered);
            button_ToggleClosed.setText("Show Closed");
        }

        // Clear right-side detail view when filter changes
        list_Requests.getSelectionModel().clearSelection();
        showRequestDetails(null);
    }

    /*******
     * <p> Method: handleMarkRead </p>
     *
     * <p> Description: Mark the currently selected request as read using
     * {@link ControllerStaffRequests#markRead(StaffRequest)}. After the
     * update, the list is refreshed and the updated request is re-selected
     * so that the bold/unbold styling and "Read:" label reflect the new
     * state. </p>
     */
    private static void handleMarkRead() {
        StaffRequest selected =
                list_Requests.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        StaffRequest updated =
                ControllerStaffRequests.markRead(selected);
        refreshRequests();
        list_Requests.getSelectionModel().select(updated);
        showRequestDetails(updated);
    }
}
