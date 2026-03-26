package guiAdminHome;

import database.Database;
import guiUserLogin.ViewUserLogin;
import guiUserUpdate.EmailAddressRecognizer;




/*******
 * <p> Title: GUIAdminHomePage Class. </p>
 * 
 * <p> Description: The Java/FX-based Admin Home Page.  This class provides the controller actions
 * basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This page contains a number of buttons that have not yet been implemented.  WHen those buttons
 * are pressed, an alert pops up to tell the user that the function associated with the button has
 * not been implemented. Also, be aware that What has been implemented may not work the way the
 * final product requires and there maybe defects in this code.
 * 
 * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-08-17 Initial version
 *  
 */

public class ControllerAdminHome {
	
	/*-*******************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	/**********
	 * <p> 
	 * 
	 * Title: performInvitation () Method. </p>
	 * 
	 * <p> Description: Protected method to send an email inviting a potential user to establish
	 * an account and a specific role. </p>
	 */
	protected static void performInvitation () {
		// Verify that the email address is valid - If not alert the user and return
		String emailAddress = ViewAdminHome.text_InvitationEmailAddress.getText();
		String emailError = EmailAddressRecognizer.checkEmailAddress(emailAddress);
    	if (!emailError.isEmpty()) {
    	    ViewUserLogin.alertUsernamePasswordError.setContentText(
    	        "Invalid email address: " + emailError);
    	    ViewUserLogin.alertUsernamePasswordError.showAndWait();
    	    return;
    	}
		
		// Check to ensure that we are not sending a second message with a new invitation code to
		// the same email address.  
		if (theDatabase.emailaddressHasBeenUsed(emailAddress)) {
			ViewAdminHome.alertEmailError.setContentText(
					"An invitation has already been sent to this email address.");
			ViewAdminHome.alertEmailError.showAndWait();
			return;
		}
		
		// Inform the user that the invitation has been sent and display the invitation code
		String theSelectedRole = (String) ViewAdminHome.combobox_SelectRole.getValue();
		String invitationCode = theDatabase.generateInvitationCode(emailAddress,
				theSelectedRole);
		String msg = "Code: " + invitationCode + " for role " + theSelectedRole + 
				" was sent to: " + emailAddress;
		System.out.println(msg);
		ViewAdminHome.alertEmailSent.setContentText(msg);
		ViewAdminHome.alertEmailSent.showAndWait();
		
		// Update the Admin Home pages status
		ViewAdminHome.text_InvitationEmailAddress.setText("");
		ViewAdminHome.label_NumberOfInvitations.setText("Number of outstanding invitations: " + 
				theDatabase.getNumberOfInvitations());
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: manageInvitations () Method. </p>
	 * 
	 * <p> Description: Protected method that is currently a stub informing the user that
	 * this function has not yet been implemented. </p>
	 */
	protected static void manageInvitations () {
		System.out.println("\n*** WARNING ***: Manage Invitations Not Yet Implemented");
		ViewAdminHome.alertNotImplemented.setTitle("*** WARNING ***");
		ViewAdminHome.alertNotImplemented.setHeaderText("Manage Invitations Issue");
		ViewAdminHome.alertNotImplemented.setContentText("Manage Invitations Not Yet Implemented");
		ViewAdminHome.alertNotImplemented.showAndWait();
	}
	
	/**********
	 * <p>
	 * Title: setOnetimePassword() Method
	 * </p>
	 *
	 * <p>
	 * Description: Protected method that allows an admin to set a one time
	 * password for a user who has forgotten their password. The one time password
	 * is printed to the console.
	 * </p>
	 */
	protected static void setOnetimePassword() {
	    String oneTimePassword = theDatabase.generateOneTimePassword();

	    String msg = "Generated one time password: " + oneTimePassword;
	    System.out.println(msg);

	    ViewAdminHome.alertEmailSent.setTitle("One Time Password");
	    ViewAdminHome.alertEmailSent.setHeaderText("Single User Password created");
	    ViewAdminHome.alertEmailSent.setContentText(msg);
	    ViewAdminHome.alertEmailSent.showAndWait();
	}



	
	/**********
	 * <p> 
	 * 
	 * Title: deleteUser () Method. </p>
	 * 
	 * <p> Description: Protected method that is currently a stub informing the user that
	 * this function has not yet been implemented. </p>
	 */
	protected static void deleteUser() {
	    guiDeleteUser.ViewDeleteUser.displayDeleteUser(ViewAdminHome.theStage,ViewAdminHome.theUser);
	}


	
/**
 * This is here to safely handle potential null strings.
 *
 * <p>This makes sure that when we are building display text, we will never run
 * into a NullPointerException if a user field (like first name, last name, or email)
 * happen to be null in the database. Null values are instead replaced with
 * an empty string.</p>
 *
 * @param s the input string, possibly null
 * @return the original string if not null, or an empty string if null
 */
private static String nz(String s) { 
    return s == null ? "" : s; 
}

/**
 * This will display a list of all users currently in the database.
 *
 * <p>This method retrieves user information, builds a formatted table
 * of their username, full name, email, and roles, and then displays that
 * information in a JavaFX box. The column widths are adjusted so that the
 * data is aligned neatly and the box is large enough to read.</p>
 *
 * <p>If no users exist, a warning dialog is displayed instead.</p>
 */
protected static void listUsers() {
    // This will retrieve the list of usernames from the database.
    var users = theDatabase.getUserList();

    // If no users (or only the placeholder "<Select a User>") exist, we alert the admin.
    if (users == null || users.size() <= 1) {
        ViewAdminHome.alertNotImplemented.setTitle("All Users");
        ViewAdminHome.alertNotImplemented.setHeaderText("No users found");
        ViewAdminHome.alertNotImplemented.setContentText("There are no user accounts to list.");
        ViewAdminHome.alertNotImplemented.showAndWait();
        return;
    }

    // Local record to represent each row of output properly.
    record Row(String u, String n, String e, String r) {}

    java.util.List<Row> rows = new java.util.ArrayList<>();
    int shown = 0;

    // Loop through each username and retrieve their information from the database.
    for (String uname : users) {
        // Skip the placeholder "<Select a User>" or null entries.
        if (uname == null || uname.startsWith("<")) continue;

        // Load full details into the Database "current user" state.
        if (!theDatabase.getUserAccountDetails(uname)) continue;

        // Build the user’s full name, making sure null values become empty strings.
        String name = (nz(theDatabase.getCurrentFirstName()) + " " + nz(theDatabase.getCurrentLastName())).trim();

        // Get their email address.
        String email = nz(theDatabase.getCurrentEmailAddress());

        // Collect all roles the user has into a list of strings.
        var roles = new java.util.ArrayList<String>();
        if (theDatabase.getCurrentAdminRole()) roles.add("Admin");
        if (theDatabase.getCurrentNewRole1()) roles.add("Student");
        if (theDatabase.getCurrentNewRole2()) roles.add("Staff");

        // Add a row for this user to the list of rows that will be displayed.
        rows.add(new Row(uname, name, email, String.join(", ", roles)));
        shown++;
    }

    // Column headers for each piece of information to be displayed
    String h1 = "Username", h2 = "Name", h3 = "Email", h4 = "Roles";

    // This calculates the maximum width of each column by comparing header length with data length.
    int w1 = Math.max(h1.length(), rows.stream().mapToInt(r -> r.u.length()).max().orElse(0));
    int w2 = Math.max(h2.length(), rows.stream().mapToInt(r -> r.n.length()).max().orElse(0));
    int w3 = Math.max(h3.length(), rows.stream().mapToInt(r -> r.e.length()).max().orElse(0));
    int w4 = Math.max(h4.length(), rows.stream().mapToInt(r -> r.r.length()).max().orElse(0));

    // This makes sure we have fixed-width columns to make sure the text aligns into a neat table.
    String fmt = "%-" + w1 + "s  %-"+ w2 + "s  %-" + w3 + "s  %-" + w4 + "s%n";

    // Divider line under headers.
    String line = "─".repeat(w1) + "  " + "─".repeat(w2) + "  " + "─".repeat(w3) + "  " + "─".repeat(w4);

    // Build the display text with headers, divider, and each user’s row.
    var sb = new StringBuilder();
    sb.append(String.format(fmt, h1, h2, h3, h4));
    sb.append(line).append('\n');
    for (Row r : rows) {
        sb.append(String.format(fmt, r.u, r.n, r.e, r.r));
    }

    // Create a TextArea to show the formatted text.
    var ta = new javafx.scene.control.TextArea(sb.toString());
    ta.setEditable(false); // Prevent editing.
    ta.setWrapText(false); // Disable wrapping for alignment purposes.
    ta.setStyle("-fx-font-family: 'monospaced'; -fx-font-size: 12px;");
    ta.setPrefWidth(900);  // Width.
    ta.setPrefHeight(500); // Height.

    // Create the window to display the users.
    var dlg = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
    dlg.setTitle("All Users");
    dlg.setHeaderText("Users: " + shown);
    dlg.getDialogPane().setContent(ta);

    // Make the size of the window large so that any content is not cut off.
    dlg.getDialogPane().setPrefWidth(950);
    dlg.getDialogPane().setPrefHeight(600);

    dlg.getButtonTypes().setAll(new javafx.scene.control.ButtonType("Return", 
            javafx.scene.control.ButtonBar.ButtonData.OK_DONE));

    // Display the window and wait for the user to close it.
    dlg.showAndWait();
}

	
	/**********
	 * <p> 
	 * 
	 * Title: addRemoveRoles () Method. </p>
	 * 
	 * <p> Description: Protected method that allows an admin to add and remove roles for any of
	 * the users currently in the system.  This is done by invoking the AddRemoveRoles Page. There
	 * is no need to specify the home page for the return as this can only be initiated by and
	 * Admin.</p>
	 */
	protected static void addRemoveRoles() {
		guiAddRemoveRoles.ViewAddRemoveRoles.displayAddRemoveRoles(ViewAdminHome.theStage, 
				ViewAdminHome.theUser);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: invalidEmailAddress () Method. </p>
	 * 
	 * <p> Description: Protected method that is intended to check an email address before it is
	 * used to reduce errors.  The code currently only checks to see that the email address is not
	 * empty.  In the future, a syntactic check must be performed and maybe there is a way to check
	 * if a properly email address is active.</p>
	 * 
	 * @param emailAddress	This String holds what is expected to be an email address
	 */
	protected static boolean invalidEmailAddress(String emailAddress) {
		if (emailAddress.length() == 0) {
			ViewAdminHome.alertEmailError.setContentText(
					"Correct the email address and try again.");
			ViewAdminHome.alertEmailError.showAndWait();
			return true;
		}
		return false;
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: performLogout () Method. </p>
	 * 
	 * <p> Description: Protected method that logs this user out of the system and returns to the
	 * login page for future use.</p>
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewAdminHome.theStage);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: performQuit () Method. </p>
	 * 
	 * <p> Description: Protected method that gracefully terminates the execution of the program.
	 * </p>
	 */
	protected static void performQuit() {
		System.exit(0);
	}
}
