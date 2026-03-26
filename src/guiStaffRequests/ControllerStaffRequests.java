package guiStaffRequests;

import applicationMain.FoundationsMain;
import database.Database;
import entityClasses.StaffRequest;
import entityClasses.User;
import guiAdminHome.ViewAdminHome;
import guiRoleStaff.ViewRoleStaffHome;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

/*******
 * <p> Title: ControllerStaffRequests Class. </p>
 * 
 * <p> Description: Controller logic for staff/admin requests. This class
 * coordinates all operations on {@link StaffRequest} objects and delegates
 * persistence to the shared {@link Database} instance. It contains no GUI
 * code; views use these methods and then update their controls based on the
 * returned {@link StaffRequest} instances. </p>
 *
 * <p> Structure and interfaces: </p>
 * <ul>
 *   <li>Uses a shared {@link Database} reference from
 *       {@link FoundationsMain#database} to load and update requests.</li>
 *   <li>Provides methods for:
 *     <ul>
 *       <li>Loading requests for a specific staff user.</li>
 *       <li>Creating and editing staff requests.</li>
 *       <li>Toggling the open/closed state and read/unread state.</li>
 *       <li>Allowing admins to update response text.</li>
 *       <li>Loading all requests for the admin view.</li>
 *     </ul>
 *   </li>
 *   <li>Most methods return updated {@link StaffRequest} instances so the
 *       caller can refresh table rows without issuing a second query.</li>
 * </ul>
 *
 * <p> Data used and produced: </p>
 * <ul>
 *   <li>Uses {@code staffRequestDB} via the database helper methods for all
 *       CRUD operations.</li>
 *   <li>Produces {@link ObservableList} instances for JavaFX table binding,
 *       and updated {@link StaffRequest} objects after each mutation.</li>
 * </ul>
 *
 * <p> Validation and testing: The behavior of this controller is validated
 * by tests such as {@code ControllerStaffRequestsTest} and admin/staff
 * integration tests. These tests verify that:
 * </p>
 * <ul>
 *   <li>requests are correctly filtered by user for staff views,</li>
 *   <li>validation in create/update methods prevents empty subjects/bodies,</li>
 *   <li>toggling and mark-read operations update the database as expected, and</li>
 *   <li>admin response updates are disallowed once a request is closed.</li>
 * </ul>
 */
public class ControllerStaffRequests {

    /** Shared database handle used for all request persistence operations. */
    private static final Database theDatabase = FoundationsMain.database;

    /*******
     * <p> Method: loadRequestsForUser </p>
     * 
     * <p> Description: Load all requests created by the given staff user.
     * This method is intended for the staff request screen, which binds the
     * returned {@link ObservableList} to its table. If the user is
     * {@code null}, an empty list is returned. </p>
     * 
     * <p> Data used and produced: </p>
     * <ul>
     *   <li>Uses {@link User#getUserName()} and
     *       {@link Database#getStaffRequestsForUser(String)} to fetch the
     *       persisted requests.</li>
     *   <li>Produces an {@link ObservableList} for direct use with JavaFX
     *       table views. </li>
     * </ul>
     *
     * <p> Validation and testing: Tested by staff-request view tests that
     * assert only the logged-in user's requests are returned and correctly
     * displayed. </p>
     * 
     * @param user the staff {@link User} whose requests should be loaded
     * @return observable list of {@link StaffRequest} instances; empty if user is null
     */
    public static ObservableList<StaffRequest> loadRequestsForUser(User user) {
        ObservableList<StaffRequest> list = FXCollections.observableArrayList();
        if (user == null) return list;
        list.addAll(theDatabase.getStaffRequestsForUser(user.getUserName()));
        return list;
    }

    /*******
     * <p> Method: createNewRequest </p>
     * 
     * <p> Description: Create a new request for the given staff user. The
     * subject and body must both be non-empty. On success, the new request
     * is inserted into the database and then reloaded so that the caller
     * receives a fully populated {@link StaffRequest} object, including any
     * auto-generated fields such as ID and timestamps. </p>
     * 
     * <p> Why this approach: Returning the reloaded {@link StaffRequest}
     * allows the view to immediately show the server-assigned ID and creation
     * time without performing a separate query. Returning {@code null} for
     * validation failures keeps the method simple for views that treat a
     * {@code null} result as "do not change the UI." </p>
     * 
     * <p> Validation and testing: Tests confirm that null users or blank
     * subject/body values result in {@code null} and no DB insert, and that
     * valid input produces a persisted {@link StaffRequest}. </p>
     * 
     * @param user    the staff {@link User} creating the request
     * @param subject subject line entered by staff
     * @param body    body text entered by staff
     * @return the created {@link StaffRequest}, or {@code null} if validation fails
     */
    public static StaffRequest createNewRequest(User user,
                                                String subject,
                                                String body) {
        if (user == null) return null;
        if (subject == null || subject.trim().isEmpty()) return null;
        if (body == null || body.trim().isEmpty()) return null;

        return theDatabase.insertStaffRequest(
                user.getUserName(),
                subject.trim(),
                body.trim());
    }

    /*******
     * <p> Method: updateRequestBody </p>
     * 
     * <p> Description: Allow staff to edit an existing request's subject
     * and body, but only while the request is still open. If the request is
     * closed or the new subject/body are blank, the original request is
     * returned unchanged. On success, the updated request is reloaded from
     * the database and returned so the view stays in sync. </p>
     *
     * <p> Why this approach: Returning the original {@link StaffRequest}
     * on validation or DB failure lets the caller keep its existing row
     * model without having to handle {@code null}. The "open only" rule
     * ensures staff cannot alter the historical record once a request has
     * been closed by staff or admin. </p>
     * 
     * <p> Validation and testing: Tests verify that:
     * <ul>
     *   <li>closed requests are not modified,</li>
     *   <li>empty subject/body values do not overwrite valid content,</li>
     *   <li>successful updates persist the new subject/body and are reflected
     *       in the reloaded {@link StaffRequest}.</li>
     * </ul>
     * </p>
     * 
     * @param r       the request to update
     * @param subject new subject text
     * @param body    new body text
     * @return the updated {@link StaffRequest}, or the original if no update occurred
     */
    public static StaffRequest updateRequestBody(StaffRequest r,
                                                 String subject,
                                                 String body) {
        if (r == null) return null;
        if (r.isClosed()) return r;
        if (subject == null || subject.trim().isEmpty()) return r;
        if (body == null || body.trim().isEmpty()) return r;

        boolean ok = theDatabase.updateStaffRequestBody(
                r.getId(), subject.trim(), body.trim());
        if (!ok) return r;
        return theDatabase.getStaffRequestById(r.getId());
    }

    /*******
     * <p> Method: toggleClosed </p>
     * 
     * <p> Description: Toggle the open/closed state for a request. This can
     * be used by staff (to close or reopen their own requests) and by admin
     * controllers via wrapper methods. If the database update fails, the
     * original request is returned. </p>
     *
     * <p> Why this approach: Toggling the value rather than passing a target
     * state from the UI keeps the controller responsible for state changes
     * and avoids subtle mismatches where the view's notion of "closed" might
     * be out of date with the database. </p>
     * 
     * <p> Validation and testing: Tests assert that calling this method
     * flips the closed flag in the database and that the returned request
     * reflects the new state. </p>
     * 
     * @param r the {@link StaffRequest} to toggle
     * @return the updated {@link StaffRequest}, or the original if DB update fails
     */
    public static StaffRequest toggleClosed(StaffRequest r) {
        if (r == null) return null;
        boolean newClosed = !r.isClosed();
        boolean ok = theDatabase.setStaffRequestClosed(r.getId(), newClosed);
        if (!ok) return r;
        return theDatabase.getStaffRequestById(r.getId());
    }

    /*******
     * <p> Method: markRead </p>
     * 
     * <p> Description: Mark the given request as read. If the request is
     * already marked as read or is {@code null}, the original request (or
     * {@code null}) is returned. On success, the reloaded request is
     * returned. </p>
     *
     * <p> Why this approach: The early return when {@code r.isRead()} is
     * already true avoids unnecessary database writes and keeps the method
     * idempotent—calling it multiple times has the same effect as calling
     * it once. </p>
     * 
     * <p> Validation and testing: Tests confirm that unread requests become
     * read in the database and that repeated calls do not alter other fields. </p>
     * 
     * @param r the {@link StaffRequest} to mark as read
     * @return the updated {@link StaffRequest}, or the original if no change was made
     */
    public static StaffRequest markRead(StaffRequest r) {
        if (r == null) return null;
        if (r.isRead()) return r;
        boolean ok = theDatabase.setStaffRequestRead(r.getId(), true);
        if (!ok) return r;
        return theDatabase.getStaffRequestById(r.getId());
    }

    /*******
     * <p> Method: adminUpdateResponse </p>
     * 
     * <p> Description: Update the admin response text for a request, but
     * only if the request is not closed. This method is intended to be
     * called from the admin controller. If the update succeeds, the
     * reloaded {@link StaffRequest} is returned. </p>
     *
     * <p> Why this approach: Using {@code trim()} and converting {@code null}
     * to an empty string ensures the database always stores a non-null value,
     * which simplifies later display logic in the GUI (no need to check for
     * {@code null} before showing the response). Disallowing edits to closed
     * requests keeps the audit trail stable once a conversation is finished. </p>
     * 
     * <p> Validation and testing: Tests verify that closed requests are not
     * changed and that valid edits update the adminReply field in the
     * database. </p>
     * 
     * @param r            the {@link StaffRequest} being updated
     * @param responseText the new admin response text
     * @return the updated {@link StaffRequest}, or the original if no update occurred
     */
    public static StaffRequest adminUpdateResponse(StaffRequest r,
                                                   String responseText) {
        if (r == null) return null;
        if (r.isClosed()) return r;

        String clean = (responseText == null ? "" : responseText.trim());
        boolean ok = theDatabase.updateStaffRequestAdminReply(r.getId(), clean);
        if (!ok) return r;
        return theDatabase.getStaffRequestById(r.getId());
    }

    /*******
     * <p> Method: loadAllRequestsForAdmin </p>
     * 
     * <p> Description: Load all staff requests for the admin screen. The
     * returned {@link ObservableList} is suitable for binding directly to a
     * JavaFX table in the admin view. </p>
     *
     * <p> Data used and produced: </p>
     * <ul>
     *   <li>Uses {@link Database#getAllStaffRequests()} to retrieve all
     *       persisted requests.</li>
     *   <li>Produces an {@link ObservableList} of {@link StaffRequest}
     *       objects. </li>
     * </ul>
     * 
     * <p> Validation and testing: Admin view tests assert that all requests
     * are visible and correctly ordered according to the underlying query. </p>
     * 
     * @return observable list of all {@link StaffRequest} instances
     */
    public static ObservableList<StaffRequest> loadAllRequestsForAdmin() {
        ObservableList<StaffRequest> list = FXCollections.observableArrayList();
        list.addAll(theDatabase.getAllStaffRequests());
        return list;
    }
    
    /*******
     * <p> Method: returnToStaffHome </p>
     * 
     * <p> Description: Navigate back to the Staff Home page for the given
     * user. This helper is intended to be called from the staff request
     * view when the user clicks a "Return" or "Home" button. </p>
     *
     * <p> Why this approach: Reusing {@link ViewRoleStaffHome} keeps
     * navigation behavior consistent with other staff workflows and avoids
     * duplicating home-page setup logic across multiple controllers. </p>
     * 
     * @param stage the current {@link Stage} used by the JavaFX application
     * @param user  the currently logged-in staff {@link User}
     */
    protected static void returnToStaffHome(Stage stage, User user) {
        ViewRoleStaffHome.displayRoleStaffHome(stage, user);
    }
}
