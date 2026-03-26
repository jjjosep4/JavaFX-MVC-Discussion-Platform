package guiUserUpdate;

import entityClasses.User;
import javafx.stage.Stage;

public class ControllerUserUpdate {
	/*-********************************************************************************************

	The Controller for ViewUserUpdate 
	
	**********************************************************************************************/

	/**********
	 * <p> Title: ControllerUserUpdate Class</p>
	 * 
	 * <p> Description: This static class supports the actions initiated by the ViewUserUpdate
	 * class. In this case, there is just one method, no constructors, and no attributes.</p>
	 *
	 */

	/*-********************************************************************************************

	The User Interface Actions for this page
	
	**********************************************************************************************/

	
	/**********
	 * <p> Method: public goToUserHomePage(Stage theStage, User theUser) </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the button to
	 * proceed to the user's home page.
	 * 
	 * @param theStage specifies the JavaFX Stage for next next GUI page and it's methods
	 * 
	 * @param theUser specifies the user so we go to the right page and so the right information
	 */
	protected static void goToUserHomePage(Stage theStage, User theUser) {

	    // Determine the home page based on the actual user role attributes
	    if (theUser.getAdminRole()) {
	        guiAdminHome.ViewAdminHome.displayAdminHome(theStage, theUser);
	        return;
	    }

	    if (theUser.getRole1()) {
	        guiRoleStudent.ViewRoleStudentHome.displayRoleStudentHome(theStage, theUser);
	        return;
	    }

	    if (theUser.getRole2()) {
	        guiRoleStaff.ViewRoleStaffHome.displayRoleStaffHome(theStage, theUser);
	        return;
	    }

	    // If all else fails – invalid user
	    System.out.println("*** ERROR *** UserUpdate: No valid role assigned to this user.");
	    System.exit(0);
	}

 	}

