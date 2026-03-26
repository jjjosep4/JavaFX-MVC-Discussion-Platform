package guiAdminRequests;

import applicationMain.FoundationsMain;
import entityClasses.StaffRequest;
import entityClasses.User;
import guiAdminHome.ViewAdminHome;
import guiStaffRequests.ControllerStaffRequests;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

/*******
 * <p> Title: ControllerAdminRequests Class. </p>
 * 
 * <p> Description: Controller for the Admin view of Staff↔Admin
 * requests.  Admins see all requests, can write responses, mark
 * them read, and open/close them.  All data is shared with the
 * staff-side ControllerStaffRequests. </p>
 */
public class ControllerAdminRequests {

    /*******
     * <p> Method: getAllRequests </p>
     * 
     * <p> Description: Return all staff requests for display on
     * the Admin Requests page. </p>
     */
    protected static ObservableList<StaffRequest> getAllRequests() {
        return ControllerStaffRequests.loadAllRequestsForAdmin();
    }

    /*******
     * <p> Method: updateAdminResponse </p>
     * 
     * <p> Description: Update the admin's response for a request.
     * Only works if the request is open (ControllerStaffRequests
     * enforces this). </p>
     */
    protected static StaffRequest updateAdminResponse(
            StaffRequest original,
            String responseText) {

        return ControllerStaffRequests.adminUpdateResponse(original, responseText);
    }

    /*******
     * <p> Method: toggleClosed </p>
     */
    protected static StaffRequest toggleClosed(
            StaffRequest original) {

        return ControllerStaffRequests.toggleClosed(original);
    }

    /*******
     * <p> Method: markRead </p>
     */
    protected static StaffRequest markRead(
            StaffRequest original) {

        return ControllerStaffRequests.markRead(original);
    }

    /*******
     * <p> Method: returnToAdminHome </p>
     * 
     * <p> Description: Navigate back to the Admin Home page. </p>
     */
    protected static void returnToAdminHome(Stage stage, User user) {
        ViewAdminHome.displayAdminHome(stage, user);
    }
}
