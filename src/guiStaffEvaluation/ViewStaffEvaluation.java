package guiStaffEvaluation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import applicationMain.FoundationsMain;
import database.Database;
import entityClasses.EvaluationParameter;
import entityClasses.User;
import guiRoleStaff.ViewRoleStaffHome;
import guiStaffParameters.ControllerStaffParameters;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/*******
 * <p> Title: ViewStaffEvaluation Class. </p>
 *
 * <p> Description: JavaFX view for staff to evaluate students based on the
 * configured evaluation parameters. This screen allows staff to:</p>
 * <ul>
 *     <li>Select a student from a table of eligible students.</li>
 *     <li>Enter scores for each evaluation parameter (rubric criterion).</li>
 *     <li>View a running total of the student's score relative to the total
 *         weight of all parameters.</li>
 *     <li>Save or clear scores, and navigate back, log out, or quit.</li>
 * </ul>
 *
 * <p> Structure and interfaces: </p>
 * <ul>
 *     <li>Uses {@link #displayStaffEvaluation(Stage, User)} as the entry point
 *         invoked by staff navigation controllers.</li>
 *     <li>Maintains static references to the shared {@link Stage}, root
 *         {@link Pane}, and {@link Scene} to mirror the structure used in
 *         other FoundationsF25 views (e.g., role home pages).</li>
 *     <li>Delegates business logic and database operations to
 *         {@link ControllerStaffEvaluation} while this class focuses on view
 *         layout and event handling.</li>
 * </ul>
 *
 * <p> Data used and produced: </p>
 * <ul>
 *     <li>Uses window dimensions from {@link FoundationsMain} to keep layout
 *         consistent across all major screens.</li>
 *     <li>Uses the logged-in {@link User} to populate user details and
 *         identify the staff member creating evaluations.</li>
 *     <li>Uses {@link ControllerStaffEvaluation} to obtain {@link User}
 *         records for students and {@link EvaluationParameter} records for
 *         rubric parameters.</li>
 *     <li>Produces evaluation requests (via {@link ControllerStaffEvaluation#saveEvaluation(User, Map, String)})
 *         and shows success or error messages via modal alerts.</li>
 * </ul>
 *
 * <p> Validation and testing: The correctness of this view is validated by
 * GUI tests such as {@code ViewStaffEvaluationTest} and integration tests
 * that simulate user flows (select student, enter scores, save, and verify
 * resulting database state via controller tests like
 * {@code ControllerStaffEvaluationTest}). These tests confirm that the view
 * populates the student list and parameters correctly, enforces integer input,
 * and displays error messages returned by the controller. </p>
 */
public class ViewStaffEvaluation {

    /* Window size */
    private static double width = FoundationsMain.WINDOW_WIDTH;
    private static double height = FoundationsMain.WINDOW_HEIGHT;

    /* GUI Area 1: Title + user details */
    protected static Label label_PageTitle = new Label();
    protected static Label label_UserDetails = new Label();
    protected static Line line_Separator1 = new Line(20, 95, width - 20, 95);

    /* GUI Area 2: Students + parameters */
    protected static Label label_Students = new Label("Students");
    protected static TableView<User> table_Students = new TableView<>();
    protected static ObservableList<User> studentData =
            FXCollections.observableArrayList();

    protected static Label label_Parameters = new Label("Evaluation Parameters & Scores");

    // We build a vertical panel of parameter rows inside a ScrollPane so that
    // the UI can support a larger number of parameters without changing the
    // window size or layout of the rest of the screen.
    protected static Pane parametersPane = new Pane();
    protected static ScrollPane parametersScroll = new ScrollPane(parametersPane);

    // Each parameter is mapped to a TextField for score input. We use
    // LinkedHashMap to preserve insertion order so that the display order
    // is stable and matches the parameter order returned by the database.
    private static Map<EvaluationParameter, TextField> parameterScoreFields =
            new LinkedHashMap<>();

    protected static Label label_TotalInfo = new Label("Total: 0 / 0");

    protected static Button button_Save = new Button("Save Evaluation");
    protected static Button button_Clear = new Button("Clear Scores");

    /* GUI Area 3: Return / Logout / Quit */
    protected static Line line_Separator4 = new Line(20, 540, width - 20, 540);
    protected static Button button_Return = new Button("Return");
    protected static Button button_Logout = new Button("Logout");
    protected static Button button_Quit = new Button("Quit");

    /* Shared */
    private static ViewStaffEvaluation theView;
    private static Database theDatabase = FoundationsMain.database;

    protected static Stage theStage;
    protected static Pane theRootPane;
    protected static User theUser;

    public static Scene theStaffEvaluationScene = null;

    /*******
     * <p> Method: displayStaffEvaluation </p>
     *
     * <p> Description: Entry point for displaying the staff evaluation screen.
     * The first invocation constructs the view and scene; subsequent calls
     * reuse the same scene and refresh only user-specific and data-specific
     * content (students and parameters). </p>
     *
     * <p> Why this approach: Reusing the same {@link Scene} and layout across
     * multiple invocations avoids the overhead of rebuilding all JavaFX nodes
     * whenever staff navigates back to this screen, and maintains consistency
     * with other major views in the FoundationsF25 application. </p>
     *
     * <p> Data used and produced: </p>
     * <ul>
     *     <li>Uses the provided {@link Stage} and {@link User} as the current
     *         context.</li>
     *     <li>Populates the students table and parameter list via controller
     *         calls and attaches the configured scene to the stage. </li>
     * </ul>
     *
     * <p> Validation and testing: Tests verify that calling this method after
     * changing parameters or students reflects the updated data in the view,
     * and that the window title is set appropriately. </p>
     *
     * @param ps   primary stage
     * @param user logged-in staff user
     */
    public static void displayStaffEvaluation(Stage ps, User user) {
        theStage = ps;
        theUser = user;

        if (theView == null) {
            theView = new ViewStaffEvaluation();
        }

        if (theUser != null) {
            label_UserDetails.setText("User: " + theUser.getUserName() + "  |  Role: Staff");
        }

        refreshStudentList();
        refreshParameters();

        theStage.setScene(theStaffEvaluationScene);
        theStage.setTitle("Staff - Student Evaluation");
        theStage.show();
    }

    /*******
     * <p> Constructor: ViewStaffEvaluation() </p>
     *
     * <p> Description: Configures the overall layout, widgets, and event
     * handlers for the staff evaluation screen. This is called only once and
     * builds the {@link Scene} that is reused by subsequent calls to
     * {@link #displayStaffEvaluation(Stage, User)}. </p>
     *
     * <p> Data used and produced: </p>
     * <ul>
     *     <li>Uses window dimensions from {@link FoundationsMain}.</li>
     *     <li>Produces a fully initialized {@link Scene} stored in
     *         {@link #theStaffEvaluationScene}. </li>
     * </ul>
     *
     * <p> Validation and testing: GUI layout tests confirm that all widgets are
     * created and placed correctly, and that button handlers trigger the
     * expected controller methods. </p>
     */
    public ViewStaffEvaluation() {

        theRootPane = new Pane();
        theStaffEvaluationScene = new Scene(theRootPane, width, height);

        // Area 1
        label_PageTitle.setText("Student Evaluation");
        setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

        setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);
        setupLineUI(line_Separator1);

        // Area 2 - Students (left)
        setupLabelUI(label_Students, "Arial", 20, 300, Pos.BASELINE_LEFT, 20, 110);
        setupStudentTableUI();

        // Area 2 - Parameters (right)
        setupLabelUI(label_Parameters, "Arial", 20, 400, Pos.BASELINE_LEFT, 420, 110);

        parametersScroll.setLayoutX(420);
        parametersScroll.setLayoutY(140);
        parametersScroll.setPrefWidth(360);
        parametersScroll.setPrefHeight(320);
        parametersScroll.setFitToWidth(true);

        setupLabelUI(label_TotalInfo, "Arial", 16, 300, Pos.BASELINE_LEFT, 420, 470);

        setupButtonUI(button_Save, "Dialog", 16, 180, Pos.CENTER, 420, 500);
        button_Save.setOnAction(e -> doSaveEvaluation());

        setupButtonUI(button_Clear, "Dialog", 16, 150, Pos.CENTER, 610, 500);
        button_Clear.setOnAction(e -> clearScores());

        // Area 3 - bottom buttons
        setupLineUI(line_Separator4);

        setupButtonUI(button_Return, "Dialog", 18, 210, Pos.CENTER, 20, 555);
        button_Return.setOnAction(e -> ViewRoleStaffHome.displayRoleStaffHome(theStage, theUser));

        setupButtonUI(button_Logout, "Dialog", 18, 210, Pos.CENTER, 300, 555);
        button_Logout.setOnAction(e -> ControllerStaffEvaluation.performLogout(theStage));

        setupButtonUI(button_Quit, "Dialog", 18, 210, Pos.CENTER, 570, 555);
        button_Quit.setOnAction(e -> ControllerStaffEvaluation.performQuit());

        theRootPane.getChildren().addAll(
                label_PageTitle,
                label_UserDetails,
                line_Separator1,
                label_Students,
                table_Students,
                label_Parameters,
                parametersScroll,
                label_TotalInfo,
                button_Save,
                button_Clear,
                line_Separator4,
                button_Return,
                button_Logout,
                button_Quit
        );
    }

    /* ------------------ Student table setup ------------------ */

    /*******
     * <p> Method: setupStudentTableUI </p>
     *
     * <p> Description: Initialize the student {@link TableView} columns,
     * sizing, and data binding. Columns are configured for username and
     * full name, leveraging JavaFX's {@link PropertyValueFactory} to map
     * {@link User} properties to table cells. </p>
     *
     * <p> Why this approach: Using a {@link TableView} allows staff to see
     * all students in a scrollable and sortable list, and the two-column layout
     * (username + name) matches the information needed to select a student
     * unambiguously without cluttering the UI with extra details. </p>
     */
    @SuppressWarnings("unchecked")
    private void setupStudentTableUI() {
        table_Students.setLayoutX(20);
        table_Students.setLayoutY(140);
        table_Students.setPrefWidth(360);
        table_Students.setPrefHeight(320);

        TableColumn<User, String> colUserName = new TableColumn<>("Username");
        colUserName.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colUserName.setPrefWidth(180);

        TableColumn<User, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName")); // adjust if different
        colName.setPrefWidth(180);

        table_Students.getColumns().clear();
        table_Students.getColumns().addAll(colUserName, colName);
        table_Students.setItems(studentData);
    }

    /*******
     * <p> Method: refreshStudentList </p>
     *
     * <p> Description: Reload the list of students from the controller and
     * refresh the table's backing {@link ObservableList}. </p>
     *
     * <p> Data used and produced: </p>
     * <ul>
     *   <li>Uses {@link ControllerStaffEvaluation#getAllUsersForEvaluation()}.</li>
     *   <li>Produces an updated {@link ObservableList} reflecting the current
     *       set of students eligible for evaluation. </li>
     * </ul>
     */
    private static void refreshStudentList() {
        List<User> all = ControllerStaffEvaluation.getAllUsersForEvaluation();
        studentData.setAll(all);
    }

    /* ------------------ Parameter list setup ------------------ */

    /*******
     * <p> Method: refreshParameters </p>
     *
     * <p> Description: Reload the evaluation parameters from the controller and
     * rebuild the parameter-score entry area. Each parameter is rendered as a
     * label with its name and weight, along with a {@link TextField} for
     * entering the score. The total weight is recomputed and reflected in the
     * summary label. </p>
     *
     * <p> Why this approach: Building the parameter rows dynamically allows the
     * staff parameter management screen to add/remove parameters without any
     * changes to this view; the evaluation UI simply reflects whatever
     * parameters the controller exposes. </p>
     */
    private static void refreshParameters() {
        parametersPane.getChildren().clear();
        parameterScoreFields.clear();

        List<EvaluationParameter> params = ControllerStaffEvaluation.getAllParameters();

        double y = 10;
        int totalWeight = 0;

        for (EvaluationParameter p : params) {
            Label nameLabel = new Label(p.getName() + " (" + p.getWeight() + ")");
            nameLabel.setFont(Font.font("Arial", 14));
            nameLabel.setLayoutX(10);
            nameLabel.setLayoutY(y);

            TextField scoreField = new TextField();
            scoreField.setLayoutX(220);
            scoreField.setLayoutY(y);
            scoreField.setPrefWidth(80);

            parameterScoreFields.put(p, scoreField);

            parametersPane.getChildren().addAll(nameLabel, scoreField);

            y += 35;
            totalWeight += p.getWeight();
        }

        parametersPane.setPrefHeight(Math.max(y + 10, 320));
        label_TotalInfo.setText("Total: 0 / " + totalWeight);
    }

    /* ------------------ Helpers for scores & save ------------------ */

    /*******
     * <p> Method: clearScores </p>
     *
     * <p> Description: Clear all score input fields and recalculate the
     * displayed total. Called when the staff member presses the "Clear Scores"
     * button. </p>
     */
    private static void clearScores() {
        for (TextField tf : parameterScoreFields.values()) {
            tf.clear();
        }
        // Recalculate displayed total so that the summary label matches the now-empty fields.
        updateTotalLabel();
    }

    /*******
     * <p> Method: updateTotalLabel </p>
     *
     * <p> Description: Recompute the total score (sum of valid parameter scores)
     * and the total available weight, and update the summary label. Only scores
     * that are non-negative integers within the parameter's weight are counted
     * toward the total to avoid misleading sums when invalid values are present. </p>
     */
    private static void updateTotalLabel() {
        int totalScore = 0;
        int totalWeight = 0;

        for (Map.Entry<EvaluationParameter, TextField> entry : parameterScoreFields.entrySet()) {
            EvaluationParameter p = entry.getKey();
            TextField tf = entry.getValue();

            totalWeight += p.getWeight();
            String txt = tf.getText();
            if (txt != null && !txt.trim().isEmpty()) {
                try {
                    int val = Integer.parseInt(txt.trim());
                    if (val >= 0 && val <= p.getWeight()) {
                        totalScore += val;
                    }
                } catch (NumberFormatException ignore) {
                    // We deliberately ignore invalid values here because the controller will
                    // perform strict validation before saving. The goal is to avoid throwing
                    // while typing and to keep the total label as a soft preview.
                }
            }
        }

        label_TotalInfo.setText("Total: " + totalScore + " / " + totalWeight);
    }

    /*******
     * <p> Method: doSaveEvaluation </p>
     *
     * <p> Description: Collect the selected student and all entered scores,
     * convert them into a parameter→score map, and delegate validation and
     * persistence to {@link ControllerStaffEvaluation#saveEvaluation(User, Map, String)}.
     * Any validation errors are reported via an {@link Alert} dialog. </p>
     *
     * <p> Why this approach: Treating empty fields as a score of 0 allows staff
     * to quickly leave parameters blank when no points are awarded, without
     * having to type zeros in every box. Full validation (range checking and
     * existence of scores) is centralized in the controller to keep this view
     * focused on UI concerns. </p>
     */
    private static void doSaveEvaluation() {
        User selectedStudent = table_Students.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            showError("No student selected", "Please select a student to evaluate.");
            return;
        }

        Map<EvaluationParameter, Integer> scoreMap = new LinkedHashMap<>();

        for (Map.Entry<EvaluationParameter, TextField> entry : parameterScoreFields.entrySet()) {
            EvaluationParameter p = entry.getKey();
            TextField tf = entry.getValue();
            String txt = tf.getText();

            if (txt == null || txt.trim().isEmpty()) {
                // Treat empty as 0 so staff do not have to fill in every field explicitly.
                scoreMap.put(p, 0);
            } else {
                try {
                    int val = Integer.parseInt(txt.trim());
                    scoreMap.put(p, val);
                } catch (NumberFormatException ex) {
                    showError("Invalid score",
                              "Score for '" + p.getName() + "' must be an integer.");
                    return;
                }
            }
        }

        try {
            ControllerStaffEvaluation.saveEvaluation(
                selectedStudent,
                scoreMap,
                theUser != null ? theUser.getUserName() : "staff"
            );
            updateTotalLabel();
            showInfo("Evaluation Saved",
                     "Evaluation saved for " + selectedStudent.getUserName() + ".");
        } catch (IllegalArgumentException ex) {
            showError("Unable to Save Evaluation", ex.getMessage());
        }
    }

    /* ------------------ Small UI helpers ------------------ */

    /*******
     * <p> Method: setupLabelUI </p>
     *
     * <p> Description: Helper method to configure common font, alignment,
     * size, and position properties for {@link Label} instances. This mirrors
     * the pattern used in other views so that labels have a consistent look
     * across the application. </p>
     */
    protected static void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, double y) {
        l.setFont(Font.font(ff, f));
        l.setMinWidth(w);
        l.setAlignment(p);
        l.setLayoutX(x);
        l.setLayoutY(y);
    }

    /*******
     * <p> Method: setupButtonUI </p>
     *
     * <p> Description: Helper method to configure common font, alignment,
     * width, and position properties for {@link Button} instances. Centralizing
     * this logic avoids duplication and keeps button styling consistent. </p>
     */
    protected static void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y) {
        b.setFont(Font.font(ff, f));
        b.setMinWidth(w);
        b.setAlignment(p);
        b.setLayoutX(x);
        b.setLayoutY(y);
    }

    /*******
     * <p> Method: setupLineUI </p>
     *
     * <p> Description: Helper method to configure visual properties for
     * separator {@link Line} elements used to divide logical areas of the UI. </p>
     */
    private static void setupLineUI(Line line) {
        line.setStrokeWidth(1.5);
    }

    /*******
     * <p> Method: showError </p>
     *
     * <p> Description: Display an error dialog with the given title and
     * message. Used to report validation and persistence problems to the user. </p>
     */
    private static void showError(String title, String message) {
        Alert a = new Alert(AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    /*******
     * <p> Method: showInfo </p>
     *
     * <p> Description: Display an informational dialog with the given title
     * and message. Used to confirm successful evaluation saves. </p>
     */
    private static void showInfo(String title, String message) {
        Alert a = new Alert(AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}
