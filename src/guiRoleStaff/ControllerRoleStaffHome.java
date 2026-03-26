package guiRoleStaff;

import javafx.stage.Stage;
import entityClasses.User;

/*******
 * <p> Title: ControllerRoleStaffHome Class. </p>
 *
 * <p> Description: Controller for the Staff Home page. This class centralizes
 * navigation actions for the four main staff functions (discussion board,
 * grading parameters, student evaluation, and staff/admin requests), as well
 * as logout and quit behavior. </p>
 *
 * <p> Structure and interfaces: </p>
 * <ul>
 *     <li>All methods are {@code protected static}, mirroring the pattern used
 *         by {@code ControllerRoleStudentHome} and the Admin Home controller
 *         in the FoundationsF25 application.</li>
 *     <li>Each method accepts the current {@link javafx.stage.Stage} and, where
 *         relevant, the currently logged-in {@link entityClasses.User}.</li>
 *     <li>Navigation is delegated to corresponding {@code View*} classes in the
 *         GUI packages (e.g., {@code ViewStudentPost}, {@code ViewStaffParameters}).</li>
 * </ul>
 *
 * <p> Data used and produced: </p>
 * <ul>
 *     <li>Uses the current {@code Stage} so the application can reuse the same
 *         window when switching between staff screens, avoiding the overhead
 *         and complexity of opening new windows.</li>
 *     <li>Uses the current {@code User} object to ensure downstream views have
 *         access to the logged-in staff member's identity and role.</li>
 *     <li>Produces navigation side effects by updating the scene shown on the
 *         {@code Stage} and, in the case of quit, terminating the process. </li>
 * </ul>
 *
 * <p> Validation and testing: Navigation behavior exposed by this controller is
 * validated by the GUI tests for the staff role in the TP3 test suite (for
 * example, tests that exercise Staff Home buttons and confirm that the correct
 * views are displayed after each action). These tests ensure that each method
 * delegates to the correct {@code View*} class and that the {@code Stage} and
 * {@code User} are passed through correctly. </p>
 */
public class ControllerRoleStaffHome {

    /*******
     * <p> Method: performLogout </p>
     *
     * <p> Description: Log the current user out and return to the login screen
     * while reusing the existing {@code Stage}. This matches the navigation
     * pattern used by the student and admin home controllers so that all roles
     * experience consistent behavior when logging out. </p>
     *
     * <p> Data used and produced: </p>
     * <ul>
     *     <li>Uses the current {@code Stage} to swap the scene back to the
     *         login UI.</li>
     *     <li>Produces a new login view on the same window, avoiding the
     *         creation of extra {@code Stage} instances. </li>
     * </ul>
     *
     * @param stage The current {@link javafx.stage.Stage} to reuse for the login UI.
     */
    protected static void performLogout(Stage stage) {
        guiUserLogin.ViewUserLogin.displayUserLogin(stage);
    }

    /*******
     * <p> Method: performQuit </p>
     *
     * <p> Description: Terminate the application immediately. This provides a
     * single, explicit exit point for the Staff Home screen that matches the
     * behavior implemented for other roles in the FoundationsF25 application. </p>
     *
     * <p> Why this approach: The method uses {@code System.exit(0)} instead of
     * attempting a more granular shutdown sequence because the application is
     * designed as a single-user, single-window desktop program. A clean JVM
     * termination is simple, predictable, and consistent with the existing
     * controllers. </p>
     */
    protected static void performQuit() {
        System.exit(0);
    }

    /*******
     * <p> Method: openStaffDiscussionBoard </p>
     *
     * <p> Description: Navigate to the Staff Discussion Board screen. This
     * reuses the same {@code Stage} and passes along the logged-in staff
     * {@code User} so that the discussion view can enforce role-specific
     * behavior (e.g., staff tools, grading capabilities). </p>
     *
     * <p> Data used and produced: </p>
     * <ul>
     *     <li>Uses the current {@code Stage} and {@code User}.</li>
     *     <li>Produces a new discussion-board view presented on the same window.</li>
     * </ul>
     *
     * @param stage The {@link javafx.stage.Stage} to use for displaying the board.
     * @param user  The currently logged-in staff {@link entityClasses.User}.
     */
    protected static void openStaffDiscussionBoard(Stage stage, User user) {
        // Lightweight console log is used here instead of a logging framework
        // to stay consistent with the existing FoundationsF25 controllers and
        // avoid pulling in additional dependencies just for navigation traces.
        System.out.println("[StaffHome] Opening Staff Discussion Board...");

        guiStudentPost.ViewStudentPost.displayStudentPost(stage, user);
    }

    /*******
     * <p> Method: openStaffParameterCrud </p>
     *
     * <p> Description: Navigate to the staff parameter CRUD screen, where staff
     * can create, edit, and delete grading parameters used to evaluate student
     * discussions. </p>
     *
     * <p> Data used and produced: </p>
     * <ul>
     *     <li>Uses the current {@code Stage} and staff {@code User}.</li>
     *     <li>Produces the parameter-management UI on the same window. </li>
     * </ul>
     *
     * @param stage The {@link javafx.stage.Stage} to use.
     * @param user  The currently logged-in staff {@link entityClasses.User}.
     */
    protected static void openStaffParameterCrud(Stage stage, User user) {
        System.out.println("[StaffHome] Opening Staff Parameter CRUD...");

        guiStaffParameters.ViewStaffParameters.displayStaffParameters(stage, user);
    }

    /*******
     * <p> Method: openStaffEvaluation </p>
     *
     * <p> Description: Navigate to the student evaluation screen, where staff
     * can assign scores to students using the configured evaluation parameters. </p>
     *
     * <p> Data used and produced: </p>
     * <ul>
     *     <li>Uses the current {@code Stage} and staff {@code User}.</li>
     *     <li>Produces the evaluation UI that allows grading operations. </li>
     * </ul>
     *
     * @param stage The {@link javafx.stage.Stage} to use.
     * @param user  The currently logged-in staff {@link entityClasses.User}.
     */
    protected static void openStaffEvaluation(Stage stage, User user) {
        guiStaffEvaluation.ViewStaffEvaluation.displayStaffEvaluation(stage, user);
    }

    /*******
     * <p> Method: openAdminRequests </p>
     *
     * <p> Description: Navigate to the staff ↔ admin requests page, where staff
     * can submit new requests to administrators and view replies, and where
     * admins can review and respond. </p>
     *
     * <p> Data used and produced: </p>
     * <ul>
     *     <li>Uses the current {@code Stage} and staff {@code User}.</li>
     *     <li>Produces the staff/admin request management UI. </li>
     * </ul>
     *
     * @param stage The {@link javafx.stage.Stage} to use.
     * @param user  The currently logged-in staff {@link entityClasses.User}.
     */
    protected static void openAdminRequests(Stage stage, User user) {
        System.out.println("[StaffHome] Opening Staff/Admin Requests...");

        guiStaffRequests.ViewStaffRequests.displayStaffRequests(stage, user);
    }
}
