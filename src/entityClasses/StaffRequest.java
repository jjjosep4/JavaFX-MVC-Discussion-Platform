package entityClasses;

import java.time.LocalDateTime;

/*******
 * <p> Title: StaffRequest Class. </p>
 *
 * <p> Description: Represents a single staff → admin request persisted in the
 * {@code staffRequestDB} table. Each instance corresponds to one database row
 * and is used as a data transfer object (DTO) between the database layer and
 * both the staff-facing and admin-facing request screens. </p>
 *
 * <p> Structure and interfaces: </p>
 * <ul>
 *     <li>Fields: {@code id}, {@code senderUserName}, {@code subject},
 *         {@code body}, {@code adminReply}, {@code closed}, {@code read},
 *         {@code createdAt} mirror the database columns.</li>
 *     <li>Constructors: one fully-populated constructor used by the DAO when
 *         mapping from {@code ResultSet}, and one convenience constructor used
 *         by the UI when creating a new request prior to database insert.</li>
 *     <li>Getters: provide read-only access for UI rendering and higher-level
 *         logic (e.g., filtering by status or user).</li>
 *     <li>Setters: used primarily by the database layer or controller code to
 *         attach generated IDs, update admin replies, and toggle status flags.</li>
 * </ul>
 *
 * <p> Data used and produced: </p>
 * <ul>
 *     <li>Used by database helper methods such as
 *         {@code insertStaffRequest}, {@code getStaffRequestById},
 *         {@code getStaffRequestsForUser}, and {@code getAllStaffRequests}
 *         to move data between the {@code staffRequestDB} table and the UI.</li>
 *     <li>Produces structured request information (subject, body, reply, status,
 *         and timestamp) that drives the staff/admin request management screens
 *         and any audit/reporting views. </li>
 * </ul>
 *
 * <p> Validation and testing: The behavior of this class (construction, field
 * mapping, and accessors/mutators) is validated by the JUnit tests associated
 * with the staff request feature, for example {@code StaffRequestTest} and
 * the database integration tests that exercise {@code mapRowToStaffRequest}
 * in the DAO. These tests confirm that {@code StaffRequest} objects round-trip
 * correctly between the database and the user interface. </p>
 */
public class StaffRequest {

    private int id;
    private String senderUserName;
    private String subject;
    private String body;
    private String adminReply;
    private boolean closed;
    private boolean read;
    private LocalDateTime createdAt;

    /*******
     * <p> Constructor: StaffRequest(int, String, String, String, String, boolean, boolean, LocalDateTime) </p>
     *
     * <p> Description: Constructs a fully-populated {@code StaffRequest} with all
     * fields explicitly specified. This constructor is used primarily by the
     * database layer (for example, in {@code mapRowToStaffRequest}) when mapping
     * a {@code ResultSet} row into an in-memory object that exactly mirrors the
     * corresponding {@code staffRequestDB} record. </p>
     *
     * <p> Data used and produced: Takes column values read from the database and
     * produces a {@code StaffRequest} that can be rendered by the staff/admin UI
     * or further processed by business logic (e.g., filtering by status). </p>
     *
     * <p> Validation and testing: Field mapping and construction are validated by
     * staff-request-related JUnit tests (e.g., {@code StaffRequestTest} and DAO
     * integration tests) that assert the object fields match the database values
     * used to populate them. </p>
     *
     * @param id          the database primary key for this request
     * @param senderUserName the username of the staff member who created the request
     * @param subject     the short subject line describing the request
     * @param body        the full body text of the request
     * @param adminReply  the administrator's reply text (may be empty)
     * @param closed      {@code true} if the request has been closed by an admin
     * @param read        {@code true} if the staff member has marked the request as read
     * @param createdAt   the timestamp when the request was created
     */
    public StaffRequest(int id,
                        String senderUserName,
                        String subject,
                        String body,
                        String adminReply,
                        boolean closed,
                        boolean read,
                        LocalDateTime createdAt) {
        this.id = id;
        this.senderUserName = senderUserName;
        this.subject = subject;
        this.body = body;
        this.adminReply = adminReply;
        this.closed = closed;
        this.read = read;
        this.createdAt = createdAt;
    }

    /*******
     * <p> Constructor: StaffRequest(String, String, String) </p>
     *
     * <p> Description: Convenience constructor used when a new request is created
     * in code (typically from the staff UI) before the database has generated an
     * auto-incremented ID or {@code createdAt} timestamp. </p>
     *
     * <p> Why this approach: The constructor deliberately passes {@code 0} for
     * {@code id} and {@code null} for {@code createdAt} so that the DAO can rely
     * on the database to generate the canonical identifier and creation time when
     * {@code insertStaffRequest} is called. This avoids guessing timestamps on the
     * client side and keeps all persistent identifiers and times consistent with
     * the database server. </p>
     *
     * <p> Validation and testing: The default values set here (empty admin reply,
     * {@code closed == false}, {@code read == false}) are validated by the staff
     * request JUnit tests, which verify that newly constructed objects match the
     * default state expected by the insertion logic. </p>
     *
     * @param senderUserName the username of the staff member who is creating the request
     * @param subject        the short subject line describing the request
     * @param body           the full body text of the request
     */
    public StaffRequest(String senderUserName,
                        String subject,
                        String body) {
        // Use neutral defaults so the DAO can attach the real id and createdAt
        // from the database after insert, while starting with known status flags.
        this(0, senderUserName, subject, body, "", false, false, null);
    }

    /*******
     * <p> Method: int getId() </p>
     *
     * <p> Description: Returns the database primary key associated with this
     * staff request. This value is used by the DAO and controllers when issuing
     * update or delete operations on {@code staffRequestDB}. </p>
     *
     * <p> Validation and testing: Getter behavior is covered by staff request
     * unit tests that construct objects with known IDs and confirm that
     * {@code getId()} returns the same value. </p>
     *
     * @return the database primary key of this request
     */
    public int getId() {
        return id;
    }

    /*******
     * <p> Method: String getSenderUserName() </p>
     *
     * <p> Description: Returns the username of the staff member who created this
     * request. The caller can use this to filter requests by owner or display the
     * requester’s identity on the admin screen. </p>
     *
     * @return the sender's username
     */
    public String getSenderUserName() {
        return senderUserName;
    }

    /*******
     * <p> Method: String getSubject() </p>
     *
     * <p> Description: Returns the short subject line summarizing this request. </p>
     *
     * @return the subject text
     */
    public String getSubject() {
        return subject;
    }

    /*******
     * <p> Method: String getBody() </p>
     *
     * <p> Description: Returns the full body text of the staff member's request. </p>
     *
     * @return the body text
     */
    public String getBody() {
        return body;
    }

    /*******
     * <p> Method: String getAdminReply() </p>
     *
     * <p> Description: Returns the administrator's reply text associated with
     * this request. This may be an empty string if no reply has been recorded. </p>
     *
     * @return the admin reply text
     */
    public String getAdminReply() {
        return adminReply;
    }

    /*******
     * <p> Method: boolean isClosed() </p>
     *
     * <p> Description: Indicates whether this request has been closed by an
     * administrator. Closed requests are typically filtered or rendered
     * differently in the UI from open requests. </p>
     *
     * @return true if the request is closed, false otherwise
     */
    public boolean isClosed() {
        return closed;
    }

    /*******
     * <p> Method: boolean isRead() </p>
     *
     * <p> Description: Indicates whether this request has been marked as read
     * by the staff member. This flag supports unread-count badges and similar
     * UI features. </p>
     *
     * @return true if the request is marked as read, false otherwise
     */
    public boolean isRead() {
        return read;
    }

    /*******
     * <p> Method: LocalDateTime getCreatedAt() </p>
     *
     * <p> Description: Returns the timestamp when this request was created,
     * as stored in the {@code staffRequestDB} table and mapped to a
     * {@link java.time.LocalDateTime}. </p>
     *
     * @return the creation timestamp, or null if not yet persisted
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters used after DB updates / reloads

    /*******
     * <p> Method: void setId(int id) </p>
     *
     * <p> Description: Sets the database primary key for this request. This
     * is typically invoked by the DAO after an insert when the generated key
     * needs to be attached to an existing in-memory object. </p>
     *
     * @param id the database primary key to assign
     */
    public void setId(int id) {
        this.id = id;
    }

    /*******
     * <p> Method: void setAdminReply(String adminReply) </p>
     *
     * <p> Description: Updates the administrator's reply text associated with
     * this request. Used after {@code updateStaffRequestAdminReply} is called
     * on the database. </p>
     *
     * @param adminReply the new reply text
     */
    public void setAdminReply(String adminReply) {
        this.adminReply = adminReply;
    }

    /*******
     * <p> Method: void setClosed(boolean closed) </p>
     *
     * <p> Description: Updates the closed status of this request. This setter
     * allows controllers to synchronize the in-memory object with changes made
     * via {@code setStaffRequestClosed} in the DAO. </p>
     *
     * @param closed true if the request is closed, false otherwise
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    /*******
     * <p> Method: void setRead(boolean read) </p>
     *
     * <p> Description: Updates the read status of this request. This setter
     * allows the UI to mark requests as read and keep the object consistent
     * with the database state. </p>
     *
     * @param read true if the request is read, false otherwise
     */
    public void setRead(boolean read) {
        this.read = read;
    }

    /*******
     * <p> Method: void setSubject(String subject) </p>
     *
     * <p> Description: Updates the subject line of this request. This is used
     * when a staff member edits an open request, typically in conjunction with
     * {@code updateStaffRequestBody} in the DAO. </p>
     *
     * @param subject the new subject text
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /*******
     * <p> Method: void setBody(String body) </p>
     *
     * <p> Description: Updates the body text of this request. This supports
     * editing an existing open request without creating a new row. </p>
     *
     * @param body the new body text
     */
    public void setBody(String body) {
        this.body = body;
    }

    /*******
     * <p> Method: void setCreatedAt(LocalDateTime createdAt) </p>
     *
     * <p> Description: Sets the creation timestamp for this request. This is
     * normally called by the DAO when mapping from the {@code createdAt}
     * column of the {@code staffRequestDB} table. </p>
     *
     * @param createdAt the creation timestamp to assign
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
