package guiStaffParameters;

import java.util.List;

import applicationMain.FoundationsMain;
import database.Database;
import entityClasses.EvaluationParameter;
import entityClasses.User;
import guiRoleStaff.ViewRoleStaffHome;
import guiStudentPost.ControllerStudentPost;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/*******
 * <p> Title: ViewStaffParameters Class. </p>
 *
 * <p> Description: JavaFX view that allows staff to manage evaluation
 * parameters (Create, Read, Update, Delete). The layout and widget design
 * follow the same style as the other FoundationsF25 GUI pages (for example,
 * the role home pages and the student discussion board). </p>
 *
 * <p> Staff can: </p>
 * <ul>
 *   <li>View existing parameters (name, description, weight).</li>
 *   <li>Create new parameters.</li>
 *   <li>Update the currently selected parameter.</li>
 *   <li>Delete the currently selected parameter.</li>
 * </ul>
 *
 * <p> Structure and interfaces: </p>
 * <ul>
 *   <li>{@link #displayStaffParameters(Stage, User)} is the entry point used
 *       by the staff navigation controller.</li>
 *   <li>The class maintains static references to the {@link Stage},
 *       {@link Pane}, and {@link Scene}, mirroring the pattern used in other
 *       views so that the layout is created once and reused across navigation
 *       events.</li>
 *   <li>All business rules (name non-empty, weight range, 100-point total
 *       constraint) are enforced by
 *       {@link ControllerStaffParameters}, not by this view. The view simply
 *       forwards user input and displays messages from
 *       {@link IllegalArgumentException}s thrown by the controller. </li>
 * </ul>
 *
 * <p> Data used and produced: </p>
 * <ul>
 *   <li>Uses {@link ControllerStaffParameters#getAllParameters()} to load
 *       {@link EvaluationParameter} objects into the table.</li>
 *   <li>Uses the current {@link User} to populate the "User Details" label
 *       at the top of the screen.</li>
 *   <li>Produces parameter-create/update/delete requests that the controller
 *       writes to the database.</li>
 * </ul>
 *
 * <p> Validation and testing: The behavior of this view is validated by GUI
 * tests and integration tests for the staff parameter feature
 * (for example, {@code ViewStaffParametersTest} combined with
 * {@code ControllerStaffParametersTest}). These tests verify that table
 * contents reflect the database, that button actions call the controller
 * correctly, and that error messages from the controller are presented to
 * the user via alerts. </p>
 */
public class ViewStaffParameters {

	/*-*******************************************************************************************
	 * Attributes
	 */

	// Window dimensions shared across major views so that all main screens
	// use the same size and layout grid.
	private static double width = FoundationsMain.WINDOW_WIDTH;
	private static double height = FoundationsMain.WINDOW_HEIGHT;

	// GUI Area 1: Title and user details
	protected static Label label_PageTitle = new Label();
	protected static Label label_UserDetails = new Label();

	// Separator between Area 1 and Area 2
	protected static Line line_Separator1 = new Line(20, 95, width - 20, 95);

	// GUI Area 2: Parameter table and input fields
	protected static Label label_TableTitle = new Label("Evaluation Parameters");

	protected static TableView<EvaluationParameter> table_Parameters = new TableView<>();
	protected static ObservableList<EvaluationParameter> parameterData =
			FXCollections.observableArrayList();

	protected static Label label_Name = new Label("Name:");
	protected static TextField text_Name = new TextField();

	protected static Label label_Description = new Label("Description:");
	protected static TextArea text_Description = new TextArea();

	protected static Label label_Weight = new Label("Weight (1-100):");
	protected static TextField text_Weight = new TextField();

	protected static Button button_Add = new Button("Add Parameter");
	protected static Button button_Update = new Button("Update Parameter");
	protected static Button button_Delete = new Button("Delete Parameter");

	// Separator between Area 2 and Area 3
	protected static Line line_Separator4 = new Line(20, 525, width - 20, 525);

	// GUI Area 3: Return / Logout / Quit
	protected static Button button_Return = new Button("Return");
	protected static Button button_Logout = new Button("Logout");
	protected static Button button_Quit = new Button("Quit");

	// Shared references
	private static ViewStaffParameters theView;
	// Retained for consistency with other views; all DB access still flows through
	// the controller so that validation and business rules are centralized.
	private static Database theDatabase = FoundationsMain.database;

	protected static Stage theStage;
	protected static Pane theRootPane;
	protected static User theUser;

	public static Scene theStaffParametersScene = null;

	/*-*******************************************************************************************
	 * Entry point
	 */

	/*******
	 * <p> Method: displayStaffParameters </p>
	 *
	 * <p> Description: Entry point for the staff parameter CRUD page. Sets the
	 * current stage and user, creates the scene if needed, refreshes the table
	 * contents from the controller, and shows the window. </p>
	 *
	 * <p> Why this approach: The scene and layout are created lazily and then
	 * reused on subsequent calls. This mirrors the pattern used by other
	 * role-specific views and avoids reconstructing all nodes whenever staff
	 * navigate back to this screen. </p>
	 *
	 * <p> Data used and produced: </p>
	 * <ul>
	 *   <li>Uses the {@link Stage} and {@link User} provided by the caller.</li>
	 *   <li>Populates the user details label and reloads the parameter table
	 *       via {@link #refreshParameterList()}.</li>
	 *   <li>Attaches {@link #theStaffParametersScene} to the stage. </li>
	 * </ul>
	 *
	 * @param ps   the {@link Stage} to draw into
	 * @param user the currently logged-in staff {@link User}
	 */
	public static void displayStaffParameters(Stage ps, User user) {

		theStage = ps;
		theUser = user;

		if (theView == null) {
			theView = new ViewStaffParameters();
		}

		// Update dynamic user label and table contents
		if (theUser != null) {
			label_UserDetails.setText("User: " + theUser.getUserName() + "  |  Role: Staff");
		}
		refreshParameterList();

		theStage.setScene(theStaffParametersScene);
		theStage.setTitle("Staff - Evaluation Parameters");
		theStage.show();
	}

	/*-*******************************************************************************************
	 * Constructor - sets up GUI static layout
	 */

	/*******
	 * <p> Constructor: ViewStaffParameters </p>
	 *
	 * <p> Description: Initializes the root {@link Pane} and {@link Scene},
	 * configures all static GUI elements (size, font, position), and wires
	 * event handlers for the CRUD and navigation buttons. Called only once;
	 * subsequent calls to {@link #displayStaffParameters(Stage, User)} reuse
	 * the resulting scene. </p>
	 */
	public ViewStaffParameters() {

		theRootPane = new Pane();
		theStaffParametersScene = new Scene(theRootPane, width, height);

		// GUI Area 1: Title and user info
		label_PageTitle.setText("Manage Evaluation Parameters");
		setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

		label_UserDetails.setText("User: ");
		setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);

		setupLineUI(line_Separator1);

		// GUI Area 2: Table + fields + buttons

		// Section label
		setupLabelUI(label_TableTitle, "Arial", 20, 300, Pos.BASELINE_LEFT, 20, 110);

		// Table setup
		setupTableUI();

		// Name field
		setupLabelUI(label_Name, "Arial", 16, 150, Pos.BASELINE_LEFT, 450, 140);
		setupTextFieldUI(text_Name, 450, 165, 250);

		// Description field
		setupLabelUI(label_Description, "Arial", 16, 200, Pos.BASELINE_LEFT, 450, 205);
		setupTextAreaUI(text_Description, 450, 230, 300, 120);

		// Weight field
		setupLabelUI(label_Weight, "Arial", 16, 200, Pos.BASELINE_LEFT, 450, 365);
		setupTextFieldUI(text_Weight, 450, 390, 100);

		// CRUD buttons
		setupButtonUI(button_Add, "Dialog", 16, 200, Pos.CENTER, 450, 430);
		button_Add.setOnAction(e -> doAddParameter());

		setupButtonUI(button_Update, "Dialog", 16, 200, Pos.CENTER, 450, 470);
		button_Update.setOnAction(e -> doUpdateParameter());

		setupButtonUI(button_Delete, "Dialog", 16, 200, Pos.CENTER, 450, 510);
		button_Delete.setOnAction(e -> doDeleteParameter());

		// Separator before bottom buttons
		setupLineUI(line_Separator4);

		// GUI Area 3: Return / Logout / Quit
		setupButtonUI(button_Return, "Dialog", 18, 210, Pos.CENTER, 20, 540);
		button_Return.setOnAction(e -> ViewRoleStaffHome.displayRoleStaffHome(theStage, theUser));

		setupButtonUI(button_Logout, "Dialog", 18, 210, Pos.CENTER, 300, 540);
		button_Logout.setOnAction(e -> ControllerStaffParameters.performLogout(theStage));

		setupButtonUI(button_Quit, "Dialog", 18, 210, Pos.CENTER, 570, 540);
		button_Quit.setOnAction(e -> ControllerStaffParameters.performQuit());

		// Add all widgets to the root pane
		theRootPane.getChildren().addAll(
				label_PageTitle,
				label_UserDetails,
				line_Separator1,
				label_TableTitle,
				table_Parameters,
				label_Name,
				text_Name,
				label_Description,
				text_Description,
				label_Weight,
				text_Weight,
				button_Add,
				button_Update,
				button_Delete,
				line_Separator4,
				button_Return,
				button_Logout,
				button_Quit
		);
	}

	/*-*******************************************************************************************
	 * Internal helpers
	 */

	/*******
	 * <p> Method: setupTableUI </p>
	 *
	 * <p> Description: Configure the {@link TableView} columns and selection
	 * behavior for the evaluation parameter table. Columns show name, weight,
	 * and description, and the selection listener populates the text fields
	 * with the selected parameter's values. </p>
	 *
	 * <p> Why this approach: Using a table with a selection listener allows
	 * staff to quickly select a parameter and immediately see/edit its fields
	 * without requiring separate dialogs or extra clicks. The column layout
	 * mirrors common table designs elsewhere in the FoundationsF25 GUI. </p>
	 */
	@SuppressWarnings("unchecked")
	private void setupTableUI() {
		table_Parameters.setLayoutX(20);
		table_Parameters.setLayoutY(140);
		table_Parameters.setPrefWidth(400);
		table_Parameters.setPrefHeight(350);

		TableColumn<EvaluationParameter, String> colName = new TableColumn<>("Name");
		colName.setCellValueFactory(new PropertyValueFactory<>("name"));
		colName.setPrefWidth(140);

		TableColumn<EvaluationParameter, Integer> colWeight = new TableColumn<>("Weight");
		colWeight.setCellValueFactory(new PropertyValueFactory<>("weight"));
		colWeight.setPrefWidth(80);

		TableColumn<EvaluationParameter, String> colDesc = new TableColumn<>("Description");
		colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
		colDesc.setPrefWidth(180);

		table_Parameters.getColumns().clear();
		table_Parameters.getColumns().addAll(colName, colWeight, colDesc);
		table_Parameters.setItems(parameterData);

		// When a row is selected, load its data into the text fields so that
		// staff can edit directly and then click Update. This avoids having a
		// separate edit dialog and keeps the interaction model simple.
		table_Parameters.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				text_Name.setText(newVal.getName());
				text_Description.setText(newVal.getDescription());
				text_Weight.setText(Integer.toString(newVal.getWeight()));
			}
		});
	}

	/*******
	 * <p> Method: refreshParameterList </p>
	 *
	 * <p> Description: Reload the parameter table from the controller. This
	 * method is invoked whenever a parameter is created, updated, or deleted
	 * so that the table reflects the latest database state. </p>
	 */
	private static void refreshParameterList() {
		List<EvaluationParameter> list = ControllerStaffParameters.getAllParameters();
		parameterData.setAll(list);
	}

	/*******
	 * <p> Method: clearFields </p>
	 *
	 * <p> Description: Clear the parameter editing fields (name, description,
	 * and weight). Used after successful CRUD operations or when changing
	 * selection. </p>
	 */
	private static void clearFields() {
		text_Name.clear();
		text_Description.clear();
		text_Weight.clear();
	}

	/*-*******************************************************************************************
	 * Button handlers (view -> controller)
	 */

	/*******
	 * <p> Method: doAddParameter </p>
	 *
	 * <p> Description: Handle the "Add Parameter" button. Forwards the current
	 * text field values to {@link ControllerStaffParameters#createParameter},
	 * then refreshes the table and clears the input fields on success. If the
	 * controller throws an {@link IllegalArgumentException}, this method
	 * displays the error message to the user in an alert. </p>
	 */
	private static void doAddParameter() {
		try {
			ControllerStaffParameters.createParameter(
				text_Name.getText(),
				text_Description.getText(),
				text_Weight.getText()
			);
			refreshParameterList();
			clearFields();
		} catch (IllegalArgumentException ex) {
			// The controller centralizes validation; we simply surface the message
			// to the user. This avoids duplicating business rules in the view.
			showError("Unable to Add Parameter", ex.getMessage());
		}
	}

	/*******
	 * <p> Method: doUpdateParameter </p>
	 *
	 * <p> Description: Handle the "Update Parameter" button. Retrieves the
	 * selected parameter from the table and forwards the new field values to
	 * {@link ControllerStaffParameters#updateParameter(EvaluationParameter, String, String, String)}.
	 * On success, refreshes the table and clears the fields. On failure, shows
	 * an error alert with the controller's message. </p>
	 */
	private static void doUpdateParameter() {
		EvaluationParameter selected = table_Parameters.getSelectionModel().getSelectedItem();
		try {
			ControllerStaffParameters.updateParameter(
				selected,
				text_Name.getText(),
				text_Description.getText(),
				text_Weight.getText()
			);
			refreshParameterList();
			clearFields();
		} catch (IllegalArgumentException ex) {
			showError("Unable to Update Parameter", ex.getMessage());
		}
	}

	/*******
	 * <p> Method: doDeleteParameter </p>
	 *
	 * <p> Description: Handle the "Delete Parameter" button. Obtains the
	 * selected parameter from the table and asks the controller to delete it.
	 * On success, refreshes the table and clears the fields; on failure, shows
	 * an error alert with the controller's message. </p>
	 */
	private static void doDeleteParameter() {
		EvaluationParameter selected = table_Parameters.getSelectionModel().getSelectedItem();
		try {
			ControllerStaffParameters.deleteParameter(selected);
			refreshParameterList();
			clearFields();
		} catch (IllegalArgumentException ex) {
			showError("Unable to Delete Parameter", ex.getMessage());
		}
	}

	/*-*******************************************************************************************
	 * Simple UI helpers
	 */

	/*******
	 * <p> Method: setupLabelUI </p>
	 *
	 * <p> Description: Helper method to configure common font, alignment, width,
	 * and position properties for {@link Label} instances. Centralizing this
	 * logic keeps labels across the screen consistent and mirrors the approach
	 * taken in other GUI classes. </p>
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
	 * <p> Description: Helper method to configure font, alignment, width,
	 * and position properties for {@link Button} instances so that all buttons
	 * on the screen share a consistent visual style. </p>
	 */
	protected static void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y) {
		b.setFont(Font.font(ff, f));
		b.setMinWidth(w);
		b.setAlignment(p);
		b.setLayoutX(x);
		b.setLayoutY(y);
	}

	/*******
	 * <p> Method: setupTextFieldUI </p>
	 *
	 * <p> Description: Helper to position and size a {@link TextField} used for
	 * parameter name and weight input. </p>
	 */
	private static void setupTextFieldUI(TextField t, double x, double y, double w) {
		t.setLayoutX(x);
		t.setLayoutY(y);
		t.setPrefWidth(w);
	}

	/*******
	 * <p> Method: setupTextAreaUI </p>
	 *
	 * <p> Description: Helper to position and size a {@link TextArea} used for
	 * editing the parameter description. </p>
	 */
	private static void setupTextAreaUI(TextArea t, double x, double y, double w, double h) {
		t.setLayoutX(x);
		t.setLayoutY(y);
		t.setPrefWidth(w);
		t.setPrefHeight(h);
	}

	/*******
	 * <p> Method: setupLineUI </p>
	 *
	 * <p> Description: Helper to configure visual properties for separator
	 * {@link Line} elements used to partition the GUI into logical areas. </p>
	 */
	private static void setupLineUI(Line line) {
		line.setStrokeWidth(1.5);
	}

	/*******
	 * <p> Method: showError </p>
	 *
	 * <p> Description: Display an error alert with the given title and message.
	 * This is used by the button handlers to surface validation or persistence
	 * errors from the controller. </p>
	 */
	private static void showError(String title, String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
