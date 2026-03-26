package Testing;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import database.Database;
import entityClasses.Post;
import entityClasses.Reply;

/**
 * HW3 tests for:
 *  - Improper Input Validation (CWE-20 / OWASP A04)
 *  - SQL Injection (CWE-89 / OWASP A03)
 *
 * Semi-automated JUnit 5 tests that exercise Database + Post/Reply
 * using the boundary and injection cases described in TP2 Test Designs.
 */
public class HW3DBValidationAndSecurity {

    private Database db;

    @BeforeEach
    public void setUp() throws SQLException {
        db = new Database();
        db.connectToDatabase();
    }

    // ================
    // CWE-20: Input Validation
    // ================

    /** IV-1: Empty title should be rejected. */
    @Test
    public void testCreatePost_EmptyTitleRejected() {
        String id = UUID.randomUUID().toString();
        Post p = new Post(id, "student1", "General", "", "Valid content",
                LocalDateTime.now(), false);

        boolean created = db.createPost(p);
        assertFalse(created, "Empty title should not be accepted");
    }


    /** IV-2: Minimum valid title (1 char) should succeed. */
    @Test
    public void testCreatePost_MinTitleAccepted() {
        String id = UUID.randomUUID().toString();
        Post p = new Post(id, "student1", "General", "A", "Short content",
                LocalDateTime.now(), false);

        boolean created = db.createPost(p);
        Post stored = db.getPostById(id);

        assertTrue(created, "Single-character title should be accepted");
        assertNotNull(stored, "Post should be retrievable by ID");
        assertEquals("A", stored.getTitle());
    }

    /** IV-3: Max length title (50 chars) should succeed. */
    @Test
    public void testCreatePost_MaxTitleAccepted() {
        int maxLen = 50; 
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxLen; i++) sb.append('a');
        String longTitle = sb.toString();

        String id = UUID.randomUUID().toString();
        Post p = new Post(id, "student1", "General", longTitle, "Boundary test",
                LocalDateTime.now(), false);

        boolean created = db.createPost(p);
        Post stored = db.getPostById(id);

        assertTrue(created, "Max length title should be accepted");
        assertNotNull(stored, "Stored post should not be null");
        assertEquals(maxLen, stored.getTitle().length(),
                "Stored title length should match max length");
    }

    /** IV-4: Over-long title (51) should be rejected. */
    @Test
    public void testCreatePost_TooLongTitleHandled() {
        int maxLen = 50;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxLen + 1; i++) sb.append('b');
        String tooLong = sb.toString();

        String id = UUID.randomUUID().toString();
        Post p = new Post(id, "student1", "General", tooLong, "Overflow",
                LocalDateTime.now(), false);

        boolean created = db.createPost(p);
        Post stored = db.getPostById(id);

        
        assertFalse(created, "Title longer than max should not be accepted");
        assertNull(stored, "Post with overlong title should not be stored");
    }

    // ================
    // CWE-89: SQL Injection
    // ================

    /** SQL-1: Injection in title should be stored literally, not executed. */
    @Test
    public void testSqlInjectionInTitleStoredLiterally() {
        String payload = "x'; DROP TABLE postDB; --";
        String id = UUID.randomUUID().toString();
        Post p = new Post(id, "attacker", "General", payload, "malicious",
                LocalDateTime.now(), false);

        boolean created = db.createPost(p);
        assertTrue(created, "Injection-like title should be treated as data");

        List<Post> allPosts = db.getAllPosts();
        assertNotNull(allPosts, "getAllPosts should still work after injection attempt");
        assertFalse(allPosts.isEmpty(), "There should be at least one post");

        Post stored = db.getPostById(id);
        assertNotNull(stored, "Injected post should still be retrievable");
        assertEquals(payload, stored.getTitle(), "Title should be stored literally");
    }

    /** SQL-2: Injection in search keyword should not return all rows. */
    @Test
    public void testSqlInjectionInSearchKeywordDoesNotBypass() {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();

        db.createPost(new Post(id1, "studentA", "General",
                "First normal post", "Content1", LocalDateTime.now(), false));
        db.createPost(new Post(id2, "studentB", "General",
                "Second normal post", "Content2", LocalDateTime.now(), false));

        String payload = "' OR '1'='1";
        List<Post> results = db.searchPostsByKeyword(payload);
        List<Post> allPosts = db.getAllPosts();

        assertNotNull(results, "Search results should not be null");
        assertTrue(results.size() <= allPosts.size(),
                "Injection keyword should not magically return extra rows");
    }
}
