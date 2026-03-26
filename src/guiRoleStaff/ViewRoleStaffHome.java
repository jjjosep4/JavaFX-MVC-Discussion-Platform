package guiRoleStaff;

import applicationMain.FoundationsMain;
import database.Database;
import entityClasses.User;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

/*******
 * <p> Title: ViewRoleStaffHome Class. </p>
 *
 * <p> Description: View for the Staff Home Page. This class defines the
 * JavaFX layout and widgets for the staff role entry screen and mirrors
 * the structure of the student and admin home pages used in the
 * FoundationsF25 application. The layout is split into three logical
 * GUI areas: </p>
 *
 * <ul>
 *   <li>GUI Area 1: Page title, current user details, and account update.</li>
 *   <li>GUI Area 2: Staff-specific tools (discussion, parameters, evaluation, requests).</li>
 *   <li>GUI Area 3: Logout and Quit controls.</li>
 * </ul>
 *
 * <p> Structure and interfaces: </p>
 * <ul>
 *   <li>Static attributes hold references to the shared {@link Stage},
 *       {@link Pane}, {@link Scene}, and {@link User} so that the view
 *       can be reused across navigation events without rebuilding the
 *       entire scene graph.</li>
 *   <li>{@link #displayRoleStaffHome(Stage, User)} is the entry point
 *       invoked by the controller to display this view.</li>
 *   <li>{@link #setupGUI()} initializes widget positions, sizes, and
 *       styling once, while {@link #populateUserSpecificData()} updates
 *       user-dependent text each time the view is shown.</li>
 *   <li>Helper methods ({@link #setupLabelUI(Label, double, double, double, double, String, int)},
 *       {@link #setupButtonUI(Button, double, double, double, double, String, int)},
 *       {@link #setupLineUI(Line)}) encapsulate common UI configuration
 *       to keep the layout code consistent with other role home pages.</li>
 * </ul>
 *
 * <p> Data used and produced: </p>
 * <ul>
 *   <li>Uses global window dimensions from {@link FoundationsMain} to keep
 *       sizing consistent across all role views.</li>
 *   <li>Uses the {@link User} object to populate the "User Details" label
 *       with the logged-in staff member's username and role.</li>
 *   <li>Produces a configured JavaFX {@link Scene} that is attached to
 *       the passed-in {@link Stage} to present the Staff Home Page.</li>
 * </ul>
 *
 * <p> Validation and testing: The layout and navigation behavior provided
 * by this view are validated by GUI tests for the staff role home page
 * (e.g., {@code ViewRoleStaffHomeTest} and higher-level integration tests
 * that simulate button clicks from the Staff Home screen). These tests
 * verify that the correct labels are shown for the current user and that
 * each button delegates to the appropriate controller method. </p>
 *
 * <p> Copyright:
 * Lynn Robert Carter © 2025 </p>
 *
 * @author Team 30
 * @version 1.00 2025-11-XX Initial version
 */
public class ViewRoleStaffHome {

	/*-*******************************************************************************************
	 * 
	 * Attributes
	 * 
	 */

	// These are the application values required by the user interface
	private static double width = FoundationsMain.WINDOW_WIDTH;
	private static double height = FoundationsMain.WINDOW_HEIGHT;

	// These are the widget attributes for the GUI. There are 3 areas for this GUI.

	// GUI Area 1: It informs the user about the purpose of this page, whose account is
	// being used, and a button to allow this user to update the account settings
	protected static Label label_PageTitle = new Label();
	protected static Label label_UserDetails = new Label();
	protected static Button button_UpdateThisUser = new Button("Account Update");

	// This is a separator and it is used to partition the GUI for various tasks
	protected static Line line_Separator1 = new Line(20, 95, width - 20, 95);

	// GUI Area 2: Staff-specific tools (four main actions)
	protected static Label label_StaffTools = new Label("Staff Tools");
	protected static Button button_StaffDiscussionBoard = new Button("Staff Discussion Board");
	protected static Button button_ManageParameters = new Button("Manage Evaluation Parameters");
	protected static Button button_EvaluateStudents = new Button("Evaluate Students");
	protected static Button button_AdminRequests = new Button("Admin Requests");

	// This is a separator used to partition the GUI for various tasks
	protected static Line line_Separator4 = new Line(20, 525, width - 20, 525);

	// GUI Area 3: Used for quitting the application and for logging out.
	protected static Button button_Logout = new Button("Logout");
	protected static Button button_Quit = new Button("Quit");

	// End of GUI objects

	// These attributes are used to configure the page and populate it with this user's information
	private static ViewRoleStaffHome theView; // Used to determine if instantiation is needed

	// Reference for the in-memory database so this package has access
	// Kept for consistency with other role views even though the current staff home
	// implementation does not query the database directly. This allows future
	// enhancements (e.g., staff dashboard counts) without changing the field pattern.
	private static Database theDatabase = FoundationsMain.database;

	protected static Stage theStage; // The Stage that JavaFX has established for us
	protected static Pane theRootPane; // The Pane that holds all the GUI widgets
	protected static User theUser; // The current logged in User

	private static Scene theViewRoleStaffHomeScene; // The shared Scene each invocation populates
	protected static final int theRole = 3; // Admin: 1; RoleStudent: 2; RoleStaff: 3

	/*-*******************************************************************************************
	 * 
	 * displayRoleStaffHome
	 * 
	 */

	/*******
	 * <p> Method: displayRoleStaffHome </p>
	 *
	 * <p> Description: Configure and display the Staff Home page for the given
	 * user and stage. This method serves as the public entry point for the
	 * staff role and mirrors the corresponding {@code display*Home} methods
	 * in the student and admin home views. </p>
	 *
	 * <p> Why this approach: The view and scene are created lazily and cached
	 * (via {@code theView} and {@code theViewRoleStaffHomeScene}) so that
	 * repeated navigation back to the Staff Home page does not reconstruct
	 * the entire scene graph. This matches the FoundationsF25 pattern and
	 * reduces layout overhead, which can be noticeable on slower systems. </p>
	 *
	 * <p> Data used and produced: </p>
	 * <ul>
	 *   <li>Stores the provided {@link Stage} and {@link User} in static
	 *       fields so that other methods in this class and the controller
	 *       can access them.</li>
	 *   <li>Produces a configured Staff Home {@link Scene} that is attached
	 *       to the stage and displayed. </li>
	 * </ul>
	 *
	 * <p> Validation and testing: GUI tests for the staff role home verify
	 * that the scene is initialized once, that user details are updated
	 * between invocations, and that the correct window title is set. </p>
	 *
	 * @param stage the {@link Stage} to use
	 * @param user  the current logged-in {@link User}
	 */
	public static void displayRoleStaffHome(Stage stage, User user) {

		// Store references for use by event handlers and future refreshes
		theStage = stage;
		theUser = user;

		// Only build the scene once; subsequent calls reuse the same layout
		// and simply refresh user-specific text. This avoids unnecessary
		// widget creation and keeps behavior consistent with other role views.
		if (theView == null) {
			theView = new ViewRoleStaffHome();
			theRootPane = new Pane();
			theViewRoleStaffHomeScene = new Scene(theRootPane, width, height);
			setupGUI();
		}

		populateUserSpecificData();

		theStage.setScene(theViewRoleStaffHomeScene);
		theStage.setTitle("Staff Home Page");
		theStage.show();
	}

	/*-*******************************************************************************************
	 * 
	 * setupGUI
	 * 
	 */

	/*******
	 * <p> Method: setupGUI </p>
	 *
	 * <p> Description: Configure the positions, sizes, and styles for all GUI
	 * widgets used on the Staff Home page. This method is called only once,
	 * when the view is first created, and follows the same coordinates and
	 * font conventions as the student and admin home pages to preserve a
	 * uniform look and feel. </p>
	 *
	 * <p> Data used and produced: </p>
	 * <ul>
	 *   <li>Uses the global window width/height and shared static widgets.</li>
	 *   <li>Produces a populated {@link Pane} containing all Staff Home
	 *       labels, buttons, and separators. </li>
	 * </ul>
	 *
	 * <p> Validation and testing: GUI layout tests for the staff home page
	 * verify that the expected widgets are present and that buttons are wired
	 * to the correct controller methods. </p>
	 */
	private static void setupGUI() {

		// GUI Area 1
		label_PageTitle.setText("Staff Home Page");
		setupLabelUI(label_PageTitle, 20, 20, 300, 30, "Arial", 20);

		setupLabelUI(label_UserDetails, 20, 55, 400, 25, "Arial", 16);

		setupButtonUI(button_UpdateThisUser, width - 170, 50, 150, 30, "Arial", 14);
		button_UpdateThisUser.setOnAction(e -> ViewUserUpdate.displayUserUpdate(theStage, theUser));

		setupLineUI(line_Separator1); 

		// GUI Area 2: Staff tools
		setupLabelUI(label_StaffTools, 20, 110, 200, 25, "Arial", 16);

		setupButtonUI(button_StaffDiscussionBoard, 20, 145, 250, 20, "Arial", 14);
		button_StaffDiscussionBoard.setOnAction(e -> 
			ControllerRoleStaffHome.openStaffDiscussionBoard(theStage, theUser)
		);

		setupButtonUI(button_ManageParameters, 20, 195, 250, 20, "Arial", 14);
		button_ManageParameters.setOnAction(e -> 
			ControllerRoleStaffHome.openStaffParameterCrud(theStage, theUser)
		);

		setupButtonUI(button_EvaluateStudents, 20, 245, 250, 20, "Arial", 14);
		button_EvaluateStudents.setOnAction(e -> 
			ControllerRoleStaffHome.openStaffEvaluation(theStage, theUser)
		);

		setupButtonUI(button_AdminRequests, 20, 295, 250, 20, "Arial", 14);
		button_AdminRequests.setOnAction(e -> 
			ControllerRoleStaffHome.openAdminRequests(theStage, theUser)
		);

		// Separator before logout/quit
		setupLineUI(line_Separator4);

		// GUI Area 3: Logout and Quit
		setupButtonUI(button_Logout, 20, 540, 250, 30, "Arial", 14);
		button_Logout.setOnAction(e -> ControllerRoleStaffHome.performLogout(theStage));

		setupButtonUI(button_Quit, 300, 540, 250, 30, "Arial", 14);
		button_Quit.setOnAction(e -> ControllerRoleStaffHome.performQuit());

		// Add all widgets to the root pane
		theRootPane.getChildren().addAll(
				label_PageTitle,
				label_UserDetails,
				button_UpdateThisUser,
				line_Separator1,
				label_StaffTools,
				button_StaffDiscussionBoard,
				button_ManageParameters,
				button_EvaluateStudents,
				button_AdminRequests,
				line_Separator4,
				button_Logout,
				button_Quit
		);
	}

	/*-*******************************************************************************************
	 * 
	 * populateUserSpecificData
	 * 
	 */

	/*******
	 * <p> Method: populateUserSpecificData </p>
	 *
     * <p> Description: Update any user-specific labels when the screen is
	 * displayed, such as the "User Details" label that shows the current
	 * username and role. This is called on every invocation of
	 * {@link #displayRoleStaffHome(Stage, User)} to ensure that the view
	 * reflects the currently logged-in user even if the scene is reused. </p>
	 *
	 * <p> Data used and produced: </p>
	 * <ul>
	 *   <li>Uses {@code theUser} populated by {@code displayRoleStaffHome}.</li>
	 *   <li>Produces updated label text for {@code label_UserDetails}. </li>
	 * </ul>
	 */
	private static void populateUserSpecificData() {
		if (theUser != null) {
			String details = "User: " + theUser.getUserName() + "  |  Role: Staff";
			label_UserDetails.setText(details);
		}
	}

	/*-*******************************************************************************************
	 * 
	 * Helper methods to configure GUI widgets
	 * 
	 */

	/*******
	 * <p> Method: setupLabelUI </p>
	 *
	 * <p> Description: Helper to configure basic geometry and font styling for a
	 * {@link Label}. This mirrors the helper used in other role home views so
	 * that all labels across the application have a consistent appearance. </p>
	 *
	 * @param l        the label to configure
	 * @param x        the x-coordinate of the label's layout position
	 * @param y        the y-coordinate of the label's layout position
	 * @param w        the preferred width
	 * @param h        the preferred height
	 * @param fontName the font family name
	 * @param fontSize the font size in points
	 */
	private static void setupLabelUI(Label l, double x, double y, double w, double h, String fontName, int fontSize) {
		l.setLayoutX(x);
		l.setLayoutY(y);
		l.setPrefWidth(w);
		l.setPrefHeight(h);
		l.setStyle("-fx-font-family: \"" + fontName + "\"; -fx-font-size: " + fontSize + "px;");
	}

	/*******
	 * <p> Method: setupButtonUI </p>
	 *
	 * <p> Description: Helper to configure basic geometry and font styling for a
	 * {@link Button}. Centralizing this styling logic ensures that buttons on
	 * the Staff Home page (and other role views that use the same helper) share
	 * a common look and feel. </p>
	 *
	 * @param b        the button to configure
	 * @param x        the x-coordinate of the button's layout position
	 * @param y        the y-coordinate of the button's layout position
	 * @param w        the preferred width
	 * @param h        the preferred height
	 * @param fontName the font family name
	 * @param fontSize the font size in points
	 */
	private static void setupButtonUI(Button b, double x, double y, double w, double h, String fontName, int fontSize) {
		b.setLayoutX(x);
		b.setLayoutY(y);
		b.setPrefWidth(w);
		b.setPrefHeight(h);
		b.setStyle("-fx-font-family: \"" + fontName + "\"; -fx-font-size: " + fontSize + "px;");
	}

	/*******
	 * <p> Method: setupLineUI </p>
	 *
	 * <p> Description: Helper to configure common visual properties for
	 * separator {@link Line} elements used to partition the GUI into areas. </p>
	 *
	 * @param line the line to configure
	 */
	private static void setupLineUI(Line line) {
		line.setStrokeWidth(1.5);
	}
}
