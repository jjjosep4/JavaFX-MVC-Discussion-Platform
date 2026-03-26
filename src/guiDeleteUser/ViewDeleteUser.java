package guiDeleteUser;

import java.util.List;
import database.Database;
import entityClasses.User;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/*******
 * <p> Title: GUIDeleteUserPage Class. </p>
 * 
 * <p> Description: The Java/FX-based page for deleting a user from the system. </p>
 * 
 */

public class ViewDeleteUser {

    /*-*******************************************************************************************
     * Attributes
     */

	// These are the application values required by the user interface
    private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
    private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

    // GUI Area 1: It informs the user about the purpose of this page, whose account is being used
    protected static Label label_PageTitle = new Label();
    protected static Label label_UserDetails = new Label();

    // This is a separator and it is used to partition the GUI for various tasks
    protected static Line line_Separator1 = new Line(20, 95, width - 20, 95);

    // Area 2a: This allows the admin to select a user of the system as the first step in deleting
 	// a user.  The act of selecting a user causes the change in the GUI.
    protected static Label label_SelectUser = new Label("Select a user to delete:");
    protected static ComboBox<String> combobox_SelectUser = new ComboBox<>();

    // Area 2b: After a user is selected the Admin will need to press the delete button to change the UI
    protected static Button button_DeleteUser = new Button("Delete This User");

    // This is a separator and it is used to partition the GUI for various tasks
    protected static Line line_Separator2 = new Line(20, 200, width - 20, 200);

    // GUI Area 3: This is last of the GUI areas.  It is used for quitting the application, logging
 	// out, and on other pages a return is provided so the user can return to a previous page when
 	// the actions on that page are complete.  Be advised that in most cases in this code, the 
 	// return is to a fixed page as opposed to the actual page that invoked the pages.
    protected static Button button_Return = new Button("Return");
    protected static Button button_Logout = new Button("Logout");
    protected static Button button_Quit = new Button("Quit");

    // This is the end of the GUI objects for the page.
	
 	// These attributes are used to configure the page and populate it with this user's information
    private static ViewDeleteUser theView;
    
 // Reference for the in-memory database so this package has access
    private static Database theDatabase = applicationMain.FoundationsMain.database;

    protected static Stage theStage;
    protected static Pane theRootPane;
    protected static User theUser;
    public static Scene theDeleteUserScene = null;

    protected static String theSelectedUser = "";	// The user who will be deleted

    /*-*******************************************************************************************

	Constructors
	
	*/

	/**********
	 * <p> Method: displayDeleteUser(Stage ps, User user) </p>
	 * 
	 * <p> Description: This method is the single entry point from outside this package to cause
	 * the Delete User page to be displayed.
	 * 
	 * It first sets up very shared attributes so we don't have to pass parameters.
	 * 
	 * It then checks to see if the page has been setup.  If not, it instantiates the class, 
	 * initializes all the static aspects of the GUI widgets (e.g., location on the page, font,
	 * size, and any methods to be performed).
	 * 
	 * After the instantiation, the code then populates the elements that change based on the user
	 * and the system's current state.  It then sets the Scene onto the stage, and makes it visible
	 * to the user.
	 * 
	 * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 * 
	 * @param user specifies the User whose roles will be updated
	 *
	 */
    public static void displayDeleteUser(Stage ps, User user) {
    	// Establish the references to the GUI and the current user
        theStage = ps;
        theUser = user;

        // If not yet established, populate the static aspects of the GUI by creating the 
     	// singleton instance of this class
        if (theView == null) theView = new ViewDeleteUser();
        
        label_UserDetails.setText("User: " + theUser.getUserName());
        
        // refresh the available users to delete
        List<String> userList = theDatabase.getUserList();
        userList.remove(theUser.getUserName());
        combobox_SelectUser.setItems(FXCollections.observableArrayList(userList));
        combobox_SelectUser.getSelectionModel().select(0);
        
        
        // Populate the dynamic aspects of the GUI with the data from the user and the current
     	// state of the system.  This page is different from the others.  Since there are two 
     	// modes (1: user has not been selected, and 2: user has been selected) there are two
     	// lists of widgets to be displayed.
        if (!combobox_SelectUser.getItems().isEmpty()) {
            combobox_SelectUser.getSelectionModel().select(0);
            theSelectedUser = combobox_SelectUser.getValue();
        } else {
            theSelectedUser = "";
        }

        
        ControllerDeleteUser.repaintTheWindow();
    }

    /**********
     * <p> Method: GUIDeleteUserPage() </p>
     * 
     *<p> Description: This method initializes all the elements of the graphical user interface.
	 * This method determines the location, size, font, color, and change and event handlers for
	 * each GUI object. </p>
     */
    public ViewDeleteUser() {
    	
    	// This page is used by all roles, so we do not specify the role being used		
		
    	// Create the Pane for the list of widgets and the Scene for the window
        theRootPane = new Pane();
        theDeleteUserScene = new Scene(theRootPane, width, height);
        
        // Populate the window with the title and other common widgets and set their static state

        // GUI Area 1
        label_PageTitle.setText("Delete User Page");
        setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

        label_UserDetails.setText("User: " + theUser.getUserName());
        setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);

        // GUI Area 2a
        setupLabelUI(label_SelectUser, "Arial", 20, 300, Pos.BASELINE_LEFT, 20, 130);

        setupComboBoxUI(combobox_SelectUser, "Dialog", 16, 250, 280, 125);
        List<String> userList = theDatabase.getUserList();
        userList.remove("<Select a User>");
        userList.remove(theUser.getUserName()); // admin can't delete self
        combobox_SelectUser.setItems(FXCollections.observableArrayList(userList));
        combobox_SelectUser.getSelectionModel().select(0);
        combobox_SelectUser.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> ControllerDeleteUser.doSelectUser());

        // GUI Area 2b
        setupButtonUI(button_DeleteUser, "Dialog", 16, 180, Pos.CENTER, 280, 165);
        button_DeleteUser.setOnAction((event) -> ControllerDeleteUser.performDeleteUser());

        // GUI Area 3
        setupButtonUI(button_Return, "Dialog", 18, 210, Pos.CENTER, 20, 540);
        button_Return.setOnAction((event) -> ControllerDeleteUser.performReturn());

        setupButtonUI(button_Logout, "Dialog", 18, 210, Pos.CENTER, 300, 540);
        button_Logout.setOnAction((event) -> ControllerDeleteUser.performLogout());

        setupButtonUI(button_Quit, "Dialog", 18, 210, Pos.CENTER, 570, 540);
        button_Quit.setOnAction((event) -> ControllerDeleteUser.performQuit());
        
        // This is the end of the GUI Widgets for the page
    }

    /*-*******************************************************************************************

	Helper methods used to minimizes the number of lines of code needed above
	
	*/

	/**********
	 * Private local method to initialize the standard fields for a label
	 * 
	 * @param l		The Label object to be initialized
	 * @param ff	The font to be used
	 * @param f		The size of the font to be used
	 * @param w		The width of the Button
	 * @param p		The alignment (e.g. left, centered, or right)
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
    protected static void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, double y) {
        l.setFont(Font.font(ff, f));
        l.setMinWidth(w);
        l.setAlignment(p);
        l.setLayoutX(x);
        l.setLayoutY(y);
    }

    /**********
	 * Private local method to initialize the standard fields for a button
	 * 
	 * @param b		The Button object to be initialized
	 * @param ff	The font to be used
	 * @param f		The size of the font to be used
	 * @param w		The width of the Button
	 * @param p		The alignment (e.g. left, centered, or right)
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
    protected static void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y) {
        b.setFont(Font.font(ff, f));
        b.setMinWidth(w);
        b.setAlignment(p);
        b.setLayoutX(x);
        b.setLayoutY(y);
    }

    /**********
	 * Private local method to initialize the standard fields for a ComboBox
	 * 
	 * @param c		The ComboBox object to be initialized
	 * @param ff	The font to be used
	 * @param f		The size of the font to be used
	 * @param w		The width of the ComboBox
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
    protected static void setupComboBoxUI(ComboBox<String> c, String ff, double f, double w, double x, double y) {
        c.setStyle("-fx-font: " + f + " " + ff + ";");
        c.setMinWidth(w);
        c.setLayoutX(x);
        c.setLayoutY(y);
    }
}

