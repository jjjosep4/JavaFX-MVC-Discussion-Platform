package guiUserLogin;

import database.Database;
import entityClasses.User;
import javafx.stage.Stage;


public class ControllerUserLogin {
	
	/*-********************************************************************************************

	The User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/


	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	private static Stage theStage;	
	
	/**********
	 * <p> Method: public doLogin() </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the Login button. This
	 * method checks the username and password to see if they are valid.  If so, it then logs that
	 * user in my determining which role to use.
	 * 
	 * The method reaches batch to the view page and to fetch the information needed rather than
	 * passing that information as parameters.
	 * 
	 */	
	protected static void doLogin(Stage ts) {
	    theStage = ts;
	    String username = ViewUserLogin.text_Username.getText();
	    String password = ViewUserLogin.text_Password.getText();
	    boolean loginResult = false;

	    // Validate the user name
	    String usernameErr = UserNameRecognizer.checkForValidUserName(username);
	    if (!usernameErr.isEmpty()) {
	        ViewUserLogin.alertUsernamePasswordError.setContentText(
	                "Invalid username: " + usernameErr);
	        ViewUserLogin.alertUsernamePasswordError.showAndWait();
	        return;
	    }

	    //make sure username/password is valid
	    if (theDatabase.getUserAccountDetails(username) == false) {
	        ViewUserLogin.alertUsernamePasswordError.setContentText(
	                "Incorrect username/password. Try again!");
	        ViewUserLogin.alertUsernamePasswordError.showAndWait();
	        return;
	    }
	    System.out.println("*** Username is valid");

	    if (theDatabase.validateOneTimePassword(password)) {
	        System.out.println("*** One-time password accepted");

	        // Ask for user for new password
	        while (true) {
	            String newPassword = ViewUserLogin.askForNewPassword();
	            if (newPassword == null) {
	                ViewUserLogin.alertUsernamePasswordError.setContentText(
	                        "Password not changed. You must set a new password to continue.");
	                ViewUserLogin.alertUsernamePasswordError.showAndWait();
	                return;
	            }
	            //validate the password the user is trying to set
	            String newPasswordError = PasswordModel.evaluatePassword(newPassword);
	            if (!newPasswordError.isEmpty()) {
	                // Give error message
	                ViewUserLogin.alertUsernamePasswordError.setContentText(
	                        "Invalid password: " + newPasswordError);
	                ViewUserLogin.alertUsernamePasswordError.showAndWait();
	                continue;
	            }

	            theDatabase.updatePassword(username, newPassword);
	            ViewUserLogin.showPasswordResetMessage();
	            return; // user has to log in again with the newly set password
	        }
	    }

	    //Validate password for typical login
	    String passwordError = PasswordModel.evaluatePassword(password);
	    if (!passwordError.isEmpty()) {
	        ViewUserLogin.alertUsernamePasswordError.setContentText(
	            "Invalid password: " + passwordError);
	        ViewUserLogin.alertUsernamePasswordError.showAndWait();
	        return;
	    }

	    //checking if password is matching
	    String actualPassword = theDatabase.getCurrentPassword();
	    if (password.compareTo(actualPassword) != 0) {
	        ViewUserLogin.alertUsernamePasswordError.setContentText(
	                "Incorrect username/password. Try again!");
	        ViewUserLogin.alertUsernamePasswordError.showAndWait();
	        return;
	    }
	    System.out.println("*** Password is valid for this user");
	    
	    
	    
	    User user = new User(username, password, theDatabase.getCurrentFirstName(),
	            theDatabase.getCurrentMiddleName(), theDatabase.getCurrentLastName(),
	            theDatabase.getCurrentPreferredFirstName(), theDatabase.getCurrentEmailAddress(),
	            theDatabase.getCurrentAdminRole(), theDatabase.getCurrentNewRole1(),
	            theDatabase.getCurrentNewRole2());

	    int numberOfRoles = theDatabase.getNumberOfRoles(user);
	    System.out.println("*** The number of roles: " + numberOfRoles);
	    if (numberOfRoles == 1) {
	        if (user.getAdminRole()) {
	            loginResult = theDatabase.loginAdmin(user);
	            if (loginResult) {
	                guiAdminHome.ViewAdminHome.displayAdminHome(theStage, user);
	            }
	        } else if (user.getRole1()) {
	            loginResult = theDatabase.loginRole1(user);
	            if (loginResult) {
	                guiRoleStudent.ViewRoleStudentHome.displayRoleStudentHome(theStage, user);
	            }
	        } else if (user.getRole2()) {
	            loginResult = theDatabase.loginRole2(user);
	            if (loginResult) {
	                guiRoleStaff.ViewRoleStaffHome.displayRoleStaffHome(theStage, user);
	            }
	        } else {
	            System.out.println("***** UserLogin goToUserHome request has an invalid role");
	        }
	    } else if (numberOfRoles > 1) {
	        guiMultipleRoleDispatch.ViewMultipleRoleDispatch.
	            displayMultipleRoleDispatch(theStage, user);
	    }
	}

	
		
	/**********
	 * <p> Method: setup() </p>
	 * 
	 * <p> Description: This method is called to reset the page and then populate it with new
	 * content.</p>
	 * 
	 */
	protected static void doSetupAccount(Stage theStage, String invitationCode) {
	    Database db = applicationMain.FoundationsMain.database;

	    // First check validity
	    if (!db.isInvitationValid(invitationCode)) {
	        ViewUserLogin.alertUsernamePasswordError.setContentText(
	            "Invitation code is invalid, expired, or already used.");
	        ViewUserLogin.alertUsernamePasswordError.showAndWait();
	        return;
	    }

	    // Now consume (this will mark it as used and return details)
	    String[] details = db.consumeInvitation(invitationCode);
	    if (details == null) {
	        ViewUserLogin.alertUsernamePasswordError.setContentText(
	            "Invitation code could not be redeemed.");
	        ViewUserLogin.alertUsernamePasswordError.showAndWait();
	        return;
	    }

	    String invitedEmail = details[0];
	    String invitedRole  = details[1];

	    // Now launch account setup screen
	    guiNewAccount.ViewNewAccount.displayNewAccount(theStage, invitationCode);
	}

	
	
	/**********
	 * <p> Method: public performQuit() </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the Quit button.  Doing
	 * this terminates the execution of the application.  All important data must be stored in the
	 * database, so there is no cleanup required.  (This is important so we can minimize the impact
	 * of crashed.)
	 * 
	 */	
	protected static void performQuit() {
		System.out.println("Perform Quit");
		System.exit(0);
	}	

}
