package guiStudentPost;

import applicationMain.FoundationsMain;
import database.Database;
import entityClasses.Post;
import entityClasses.Reply;
import guiDeleteUser.ViewDeleteUser;
import javafx.stage.Stage;
import java.util.List;


/*************
 *
 *	The controller attributes for this page
 *	
 *	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
 *	static methods that can be called by the View (which is a singleton instantiated object) and 
 *	the Model is often just a stub, or will be a singleton instantiated object.
 *
 *
 * @author
 * @version 2.0 - 2025-10-23
 */
public class ControllerStudentPost {
	
    
    /** Return user to student home screen after viewing posts */
    protected static void returnToHome(Stage stage) {
        guiRoleStudent.ViewRoleStudentHome.displayRoleStudentHome(
                ViewStudentPost.theStage, ViewStudentPost.theUser);
    }

    /** Log out and show login screen again */
    protected static void performLogout(Stage stage) {
        guiUserLogin.ViewUserLogin.displayUserLogin(stage);
    }

    /** Quit the entire app (called from exit button) */
    protected static void performQuit() {
        System.exit(0);
    }
}
