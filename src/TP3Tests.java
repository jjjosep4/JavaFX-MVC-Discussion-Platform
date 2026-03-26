package prototypeTesting.tp3;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import database.Database;
import entityClasses.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

/*******
 * <p> Title: TP3Tests Class. </p>
 *
 * <p> Description: JUnit regression tests for the TP3 enhancements to the
 * discussion and staff-support system. This suite exercises the new database
 * behavior added for staff features, including: </p>
 *
 * <ul>
 *   <li>Staff replies (public and private) to student posts.</li>
 *   <li>Soft-deletion of posts by staff and thread-level deletion.</li>
 *   <li>Creation and validation of evaluation parameters and student
 *       evaluation records.</li>
 *   <li>Staff ↔ admin request creation, response, and closing. </li>
 * </ul>
 *
 * <p> Structure and interfaces: </p>
 * <ul>
 *   <li>Uses JUnit 5 with {@link TestMethodOrder} and
 *       {@link MethodOrderer.OrderAnnotation} so tests run in a predictable
 *       sequence, matching the narrative of the TP3 feature list.</li>
 *   <li>Each test method focuses on a single behavior and validates the
 *       related {@link Database} methods (for example,
 *       {@code createReply}, {@code deleteThread},
 *       {@code createEvaluationParameter},
 *       {@code insertStaffRequest}, {@code updateStaffRequestAdminReply},
 *       etc.).</li>
 *   <li>All tests share a fresh in-memory database instance created in
 *       {@link #setup()} so test results do not depend on ordering or
 *       external state.</li>
 * </ul>
 *
 * <p> Data used and produced: </p>
 * <ul>
 *   <li>Uses in-memory {@link Database} tables populated with small,
 *       synthetic {@link Post}, {@link Reply}, {@link EvaluationParameter},
 *       {@link User}, and {@link StaffRequest} instances.</li>
 *   <li>Produces assertions about row counts, flags (such as <em>deleted</em>,
 *       <em>private</em>, and <em>closed</em>), and stored field values to
 *       validate that TP3 features behave as designed. </li>
 * </ul>
 *
 * <p> Validation and testing: This class itself is part of the automated
 * test suite for TP3. It is complemented by manual tests that exercise the
 * GUI workflows described in the TP3 manual test PDF (for example, staff
 * creating requests via the Staff Requests screen and admins responding
 * through their corresponding view). </p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TP3Tests {

    /** Fresh database instance used for each test case. */
    private Database db;

    /*******
     * <p> Method: setup </p>
     *
     * <p> Description: Create a new {@link Database} instance, connect to
     * the in-memory database, wipe all existing objects, and reconnect so
     * that the schema is recreated. This ensures that each test runs in a
     * clean, deterministic environment without cross-test contamination. </p>
     *
     * <p> Why this approach: Using {@code DROP ALL OBJECTS} on the open
     * connection gives us a simple way to reset both data and schema for
     * H2-based databases. Reconnecting forces the {@link Database} class to
     * re-run its table-creation logic, keeping the tests aligned with the
     * production initialization path. </p>
     *
     * @throws Exception if the database cannot be created or reset
     */
    @BeforeEach
    public void setup() throws Exception {
        db = new Database();
        db.connectToDatabase();

        // Drop all objects rather than deleting row-by-row so we can exercise
        // the same "fresh startup" path that a new user of the application sees.
        db.getConnection().createStatement().execute("DROP ALL OBJECTS");

        // Reconnect → the Database implementation recreates all tables.
        db.connectToDatabase(); 
    }


    // ------------------------------------------------------------
    // 1. Staff can reply to a student post
    // ------------------------------------------------------------

    /*******
     * <p> Method: testStaffCanReplyToStudentPost </p>
     *
     * <p> Description: Validate that a staff member can create a reply to a
     * student post and that the reply is persisted and counted correctly.
     * This test exercises {@link Database#createPost(Post)},
     * {@link Database#createReply(Reply)}, and
     * {@link Database#getReplyCountForPost(String)}. </p>
     *
     * <p> Data used and produced: Inserts a single {@link Post} with ID
     * {@code "p1"}, then inserts a {@link Reply} authored by a staff user.
     * Asserts that the reply insertion returns {@code true} and that the
     * reply count for {@code "p1"} is exactly 1. </p>
     */
    @Test
    @Order(1)
    public void testStaffCanReplyToStudentPost() {
        Post p = new Post("p1", "studentA", "General", "Test", "Content", LocalDateTime.now(), false);
        db.createPost(p);

        Reply r = new Reply("p1", "staffUser", "Good work!", false);
        boolean ok = db.createReply(r);

        assertTrue(ok);
        assertEquals(1, db.getReplyCountForPost("p1"));
    }

    // ------------------------------------------------------------
    // 2. Staff can delete a student's post
    // ------------------------------------------------------------

    /*******
     * <p> Method: testStaffCanDeleteStudentPost </p>
     *
     * <p> Description: Validate that staff can soft-delete a student's post
     * and that the deleted flag is correctly persisted. This tests
     * {@link Database#createPost(Post)},
     * {@link Database#softDeletePost(String)}, and
     * {@link Database#getPostById(String)}. </p>
     *
     * <p> Data used and produced: Inserts a post with ID {@code "p2"},
     * soft-deletes it via the database, reloads it, and asserts that the
     * {@code deleted} flag is set. </p>
     */
    @Test
    @Order(2)
    public void testStaffCanDeleteStudentPost() {
        Post p = new Post("p2", "studentB", "General", "Hello", "World", LocalDateTime.now(), false);
        db.createPost(p);

        assertTrue(db.softDeletePost("p2"));
        Post deleted = db.getPostById("p2");

        assertTrue(deleted.isDeleted());
    }

    // ------------------------------------------------------------
    // 3. Staff private feedback reply
    // ------------------------------------------------------------

    /*******
     * <p> Method: testPrivateFeedbackReply </p>
     *
     * <p> Description: Validate that a staff member can create a <em>private</em>
     * reply to a student's post, and that the {@code isPrivate} flag is
     * persisted and reloaded correctly. Tests
     * {@link Database#createPost(Post)},
     * {@link Database#createReply(Reply)}, and
     * {@link Database#getRepliesForPost(String)}. </p>
     *
     * <p> Data used and produced: Inserts a single post, then inserts a reply
     * marked as private. Reloads the reply list and asserts that the stored
     * reply is flagged as private. </p>
     */
    @Test
    @Order(3)
    public void testPrivateFeedbackReply() {
        Post p = new Post("p3", "stud", "General", "Title", "Text", LocalDateTime.now(), false);
        db.createPost(p);

        Reply feedback = new Reply("p3", "staff1", "PRIVATE FEEDBACK", true);
        assertTrue(db.createReply(feedback));

        Reply loaded = db.getRepliesForPost("p3").get(0);
        assertTrue(loaded.isPrivate());
    }

    // ------------------------------------------------------------
    // 4. Staff creates + deletes threads
    // ------------------------------------------------------------

    /*******
     * <p> Method: testStaffCreateAndDeleteThread </p>
     *
     * <p> Description: Validate the "delete thread" behavior that soft-deletes
     * all posts with a given thread/category. Exercises
     * {@link Database#createPost(Post)} and
     * {@link Database#deleteThread(String)}, as well as the soft-delete
     * behavior visible through {@link Database#getPostById(String)}. </p>
     *
     * <p> Data used and produced: Inserts one post in the {@code "Homework"}
     * thread, calls {@code deleteThread("Homework")}, and asserts that the
     * post is now marked deleted. </p>
     */
    @Test
    @Order(4)
    public void testStaffCreateAndDeleteThread() {
        Post p = new Post("p4", "staff", "Homework", "HW1", "desc", LocalDateTime.now(), false);
        db.createPost(p);

        assertTrue(db.deleteThread("Homework"));

        Post reloaded = db.getPostById("p4");
        assertTrue(reloaded.isDeleted());
    }

    // ------------------------------------------------------------
    // 5. Staff creates parameter
    // ------------------------------------------------------------

    /*******
     * <p> Method: testStaffCreatesParameter </p>
     *
     * <p> Description: Validate that staff can create an evaluation parameter
     * and that it can be read back from the database. Tests
     * {@link Database#createEvaluationParameter(String, String, int)} and
     * {@link Database#getAllEvaluationParameters()}. </p>
     *
     * <p> Data used and produced: Creates a parameter named {@code "Quality"}
     * with weight {@code 20}, then verifies that the list of parameters
     * contains exactly one entry with the expected name. </p>
     */
    @Test
    @Order(5)
    public void testStaffCreatesParameter() {
        assertTrue(db.createEvaluationParameter("Quality", "How well they write", 20));

        List<EvaluationParameter> list = db.getAllEvaluationParameters();
        assertEquals(1, list.size());
        assertEquals("Quality", list.get(0).getName());
    }

    // ------------------------------------------------------------
    // 6. Parameter cannot exceed weight 100
    // ------------------------------------------------------------

    /*******
     * <p> Method: testParameterCannotExceed100Weight </p>
     *
     * <p> Description: Validate that the total sum of evaluation parameter
     * weights cannot exceed 100. Tests that the database rejects a second
     * parameter when the combined weight would go over the limit. </p>
     *
     * <p> Data used and produced: </p>
     * <ul>
     *   <li>Creates a first parameter with weight {@code 90}.</li>
     *   <li>Attempts to add a second parameter with weight {@code 20},
     *       which would give a total of {@code 110} and must be rejected.</li>
     * </ul>
     *
     * <p> This test exercises both the weight bounds and the total-weight
     * enforcement logic in
     * {@link Database#createEvaluationParameter(String, String, int)}. </p>
     */
    @Test
    @Order(6)
    public void testParameterCannotExceed100Weight() {
        assertTrue(db.createEvaluationParameter("Q1", "aaa", 90));
        // 90 + 20 = 110 > 100 → the second create must fail to enforce the constraint.
        assertFalse(db.createEvaluationParameter("Q2", "bbb", 20));   // would total 110
    }

    // ------------------------------------------------------------
    // 7. Parameter must have title and description
    // ------------------------------------------------------------

    /*******
     * <p> Method: testParameterNeedsTitleAndDescription </p>
     *
     * <p> Description: Validate name/description rules for parameters.
     * Currently the implementation requires a non-empty name but allows an
     * empty description so staff can use short labels without verbose text
     * if desired. </p>
     *
     * <p> Data used and produced: Attempts to create a parameter with an
     * empty name (must fail) and one with a valid name but empty description
     * (must succeed). This tests the validation logic in
     * {@link Database#createEvaluationParameter(String, String, int)}. </p>
     */
    @Test
    @Order(7)
    public void testParameterNeedsTitleAndDescription() {
        assertFalse(db.createEvaluationParameter("", "desc", 10));       // no title → invalid
        assertTrue(db.createEvaluationParameter("Valid", "", 10));       // description allowed to be empty
    }

    // ------------------------------------------------------------
    // 8. Staff applies evaluation for student
    // ------------------------------------------------------------

    /*******
     * <p> Method: testStaffAppliesEvaluation </p>
     *
     * <p> Description: Validate that staff can apply an evaluation parameter
     * to a student and that a corresponding evaluation record is stored. This
     * test exercises {@link Database#register(User)},
     * {@link Database#createEvaluationParameter(String, String, int)}, and
     * {@link Database#createEvaluationRecord(String, String, int, int, String)}. </p>
     *
     * <p> Data used and produced: Registers a student user, creates one
     * evaluation parameter with weight 10, and stores a score of 7 for that
     * student and parameter. Asserts that the record insertion returns
     * {@code true}. </p>
     *
     * @throws SQLException if the underlying JDBC calls fail
     */
    @Test
    @Order(8)
    public void testStaffAppliesEvaluation() throws SQLException {
        // Insert student so that the evaluation record references a valid user.
        User student = new User("studX", "pw", "A", null, "B", null, "email", false, true, false);
        db.register(student);

        // Insert parameter and then use its stored weight and name so we
        // match what the grading screen will query from the DB.
        db.createEvaluationParameter("Skill", "Test skill", 10);
        EvaluationParameter param = db.getAllEvaluationParameters().get(0);

        boolean ok = db.createEvaluationRecord("studX", param.getName(), 7, param.getWeight(), "staff");
        assertTrue(ok);
    }

    // ------------------------------------------------------------
    // 9. Staff sends a request to Admin
    // ------------------------------------------------------------

    /*******
     * <p> Method: testStaffSendRequestToAdmin </p>
     *
     * <p> Description: Validate that a staff member can create a new request
     * to the admin and that the subject/body fields are stored correctly.
     * Tests {@link Database#insertStaffRequest(String, String, String)} and
     * the basic mapping of database rows to {@link StaffRequest}. </p>
     *
     * <p> Data used and produced: Inserts a request with a known subject and
     * body, then asserts that the returned {@link StaffRequest} is non-null
     * and its fields match the input. </p>
     */
    @Test
    @Order(9)
    public void testStaffSendRequestToAdmin() {
        StaffRequest sr = db.insertStaffRequest("staff1", "Need help", "Please check this");

        assertNotNull(sr);
        assertEquals("Need help", sr.getSubject());
        assertEquals("Please check this", sr.getBody());
    }

    // ------------------------------------------------------------
    // 10. Admin responds to Staff request
    // ------------------------------------------------------------

    /*******
     * <p> Method: testAdminRespondsToRequest </p>
     *
     * <p> Description: Validate that an admin can add a response to an
     * existing staff request and that the response is persisted. Exercises
     * {@link Database#insertStaffRequest(String, String, String)},
     * {@link Database#updateStaffRequestAdminReply(int, String)}, and
     * {@link Database#getStaffRequestById(int)}. </p>
     *
     * <p> Data used and produced: Creates a request, updates its admin reply
     * text, reloads the request by ID, and asserts that the stored reply
     * matches the expected response. </p>
     */
    @Test
    @Order(10)
    public void testAdminRespondsToRequest() {
        StaffRequest sr = db.insertStaffRequest("staff1", "Need help", "body");

        boolean ok = db.updateStaffRequestAdminReply(sr.getId(), "Here is the answer");
        assertTrue(ok);

        StaffRequest updated = db.getStaffRequestById(sr.getId());
        assertEquals("Here is the answer", updated.getAdminReply());
    }

    // ------------------------------------------------------------
    // 11. Admin closes request
    // ------------------------------------------------------------

    /*******
     * <p> Method: testAdminClosesRequest </p>
     *
     * <p> Description: Validate that an admin (or staff, depending on the UI)
     * can close a staff request and that the closed flag is stored correctly.
     * Tests {@link Database#insertStaffRequest(String, String, String)},
     * {@link Database#setStaffRequestClosed(int, boolean)}, and
     * {@link Database#getStaffRequestById(int)}. </p>
     *
     * <p> Data used and produced: Inserts a request, calls
     * {@code setStaffRequestClosed(..., true)}, reloads the request, and
     * asserts that {@code isClosed()} returns {@code true}. </p>
     */
    @Test
    @Order(11)
    public void testAdminClosesRequest() {
        StaffRequest sr = db.insertStaffRequest("staff1", "Need help", "body");

        assertTrue(db.setStaffRequestClosed(sr.getId(), true));

        StaffRequest updated = db.getStaffRequestById(sr.getId());
        assertTrue(updated.isClosed());
    }
}
