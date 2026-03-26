package guiRoleStudent;

import javafx.stage.Stage;
import guiStudentPost.ViewStudentPost;
import entityClasses.User;

/*******
 * <p> Title: ControllerRoleStudentHome </p>
 * <p> Description: Controller for the student's home page; includes navigation to discussion board. </p>
 */
public class ControllerRoleStudentHome {

    protected static void performLogout() {
        guiUserLogin.ViewUserLogin.displayUserLogin(ViewRoleStudentHome.theStage);
    }

    protected static void performQuit() {
        System.exit(0);
    }

    /** Open the Discussion Board for the student */
    protected static void openDiscussionBoard(Stage stage, User user) {
        ViewStudentPost.displayStudentPost(stage, user);
    }
}
