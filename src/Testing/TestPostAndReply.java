package Testing;

import database.Database;
import entityClasses.Post;
import entityClasses.Reply;
import java.util.List;
import java.util.UUID;

/**
 * <p><b>TestPostAndReply Class</b></p>
 *
 * <p>This class runs all the CRUD and validation tests for Posts and Replies.
 * It’s console-based (no JUnit) and shows “PASS” or “FAIL” for each test.</p>
 *
 * <p>The goal is to prove that the Student User Stories — creating, reading,
 * updating, deleting, and validating posts/replies — actually work as expected.</p>
 *
 * <p>This is consistent with the testing approach from HW2 and TP1 and provides
 * easy visual proof that the system meets CRUD and input validation requirements.</p>
 *
 * @version 1.00 (2025-10-15)
 */
public class TestPostAndReply {

    /**
     * Runs all the post and reply tests in order.  
     * Each prints a short message showing the outcome for visibility.
     *
     * @param db database object used for creating, reading, updating, deleting posts/replies
     */
    public static void runAllTests(Database db) {
        System.out.println("\n===============================");
        System.out.println("HW2 Automated Tests (Posts & Replies)");
        System.out.println("===============================\n");

        // Each test corresponds to a Student User Story requirement
        testCreateValidPost(db);   // Student creates a valid post
        testCreateInvalidPost(db); // Invalid input validation
        testUpdatePost(db);        // Student edits a post
        testDeletePost(db);        // Student deletes a post
        testSearchPosts(db);       // Searching through posts (Read functionality)
        testCreateValidReply(db);  // Create a valid reply
        testCreateInvalidReply(db);// Invalid reply test (validation)
        testMyPosts(db);           // Retrieve posts by a specific user
        testAllPosts(db);          // Retrieve all posts overall

        System.out.println("\n===============================");
        System.out.println("End of Tests");
        System.out.println("===============================");
    }

    /** 
     * Test 1 — Verifies that a valid post is successfully created and stored.  
     * Checks that the post is retrievable by its ID afterward.
     */
    private static void testCreateValidPost(Database db) {
        System.out.print("Test 1: Create Valid Post → ");
        String id = UUID.randomUUID().toString();
        Post p = new Post(id, "student1", "General", "Valid Post", "This is a valid test post.",
                java.time.LocalDateTime.now(), false);
        boolean success = db.createPost(p);

        // Expecting the database to save it properly
        if (success && db.getPostById(id) != null)
            System.out.println("PASS");
        else
            System.out.println("FAIL (Post not saved or ID not found)");
    }

    /** 
     * Test 2 — Checks that an invalid (empty) post is rejected.  
     * This validates proper input checking for title/content.
     */
    private static void testCreateInvalidPost(Database db) {
        System.out.print("Test 2: Create Invalid (Empty) Post → ");
        // Missing title and content → should fail
        String id = UUID.randomUUID().toString();
        Post p = new Post(id, "student1", "General", "", "", java.time.LocalDateTime.now(), false);
        boolean success = db.createPost(p);

        if (!success)
            System.out.println("PASS");
        else
            System.out.println("FAIL (Empty post accepted)");
    }
    
    /** 
     * Test 3 — Simulates updating a post’s content and verifies the change.
     * Ensures that edits actually replace the previous text.
     */
    private static void testUpdatePost(Database db) {
        System.out.print("Test 3: Update Post Content → ");
        String id = UUID.randomUUID().toString();
        Post p = new Post(id, "student2", "General", "Update Test", "Old Content",
                java.time.LocalDateTime.now(), false);
        db.createPost(p);

        // Perform update → should change content
        boolean updated = db.updatePost(id, "Update Test", "New Updated Content");
        Post updatedPost = db.getPostById(id);

        if (updated && updatedPost != null && updatedPost.getContent().contains("Updated"))
            System.out.println("PASS");
        else
            System.out.println("FAIL (Update failed or incorrect content)");
    }

    /** 
     * Test 4 — Verifies “Delete” functionality using soft delete.  
     * Post should be flagged as deleted but not removed from DB.
     */
    private static void testDeletePost(Database db) {
        System.out.print("Test 4: Delete Post (Soft Delete) → ");
        String id = UUID.randomUUID().toString();
        Post p = new Post(id, "student3", "General", "Delete Test", "To be deleted",
                java.time.LocalDateTime.now(), false);
        db.createPost(p);

        boolean deleted = db.softDeletePost(id);
        Post result = db.getPostById(id);

        // Expect deleted flag to be true but post still retrievable
        if (deleted && result != null && result.isDeleted())
            System.out.println("PASS");
        else
            System.out.println("FAIL (Soft delete not applied)");
    }

    /** 
     * Test 5 — Checks keyword search to confirm Read/Filter functionality works.
     * Should only return the matching post(s) containing the keyword.
     */
    private static void testSearchPosts(Database db) {
        System.out.print("Test 5: Search Posts by Keyword → ");
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        db.createPost(new Post(id1, "student4", "General", "JavaFX Testing", "GUI example",
                java.time.LocalDateTime.now(), false));
        db.createPost(new Post(id2, "student4", "General", "Database Query", "SQL test content",
                java.time.LocalDateTime.now(), false));

        // Search should return only one relevant post
        List<Post> results = db.searchPostsByKeyword("javafx");
        if (results.size() == 1 && results.get(0).getTitle().equals("JavaFX Testing"))
            System.out.println("PASS");
        else
            System.out.println("FAIL (Keyword search returned incorrect results)");
    }

    /** 
     * Test 6 — Confirms that a valid reply can be added to a post and retrieved later.  
     * Verifies parent-child relationship between posts and replies.
     */
    private static void testCreateValidReply(Database db) {
        System.out.print("Test 6: Create Valid Reply → ");
        String postId = UUID.randomUUID().toString();
        Post parent = new Post(postId, "student5", "General", "Parent Post", "Parent content",
                java.time.LocalDateTime.now(), false);
        db.createPost(parent);

        // Valid reply → should be accepted
        String replyId = UUID.randomUUID().toString();
        Reply r = new Reply(replyId, postId, "student6", "This is a valid reply.",
                java.time.LocalDateTime.now(), false);
        boolean created = db.createReply(r);

        List<Reply> replies = db.getRepliesForPost(postId);
        if (created && !replies.isEmpty() && replies.get(0).getMessage().contains("valid"))
            System.out.println("PASS");
        else
            System.out.println("FAIL (Reply not added or not retrievable)");
    }

    /** 
     * Test 7 — Tests validation on replies by trying to submit an empty message.
     * Expected behavior: rejection by the database layer.
     */
    private static void testCreateInvalidReply(Database db) {
        System.out.print("Test 7: Create Invalid (Empty) Reply → ");
        String postId = UUID.randomUUID().toString();
        Post parent = new Post(postId, "student7", "General", "Reply Validation", "Testing empty reply",
                java.time.LocalDateTime.now(), false);
        db.createPost(parent);

        String replyId = UUID.randomUUID().toString();
        Reply r = new Reply(replyId, postId, "student8", "", java.time.LocalDateTime.now(), false);
        boolean created = db.createReply(r);

        if (!created)
            System.out.println("PASS");
        else
            System.out.println("FAIL (Empty reply accepted)");
    }

    /** 
     * Test 8 — Retrieves only posts made by a single user (Student Story: “My Posts”).  
     * Ensures the system filters results correctly by username.
     */
	private static void testMyPosts(Database db) {
		System.out.print("Test 8: Get My Posts → ");
		db.createPost(new Post(UUID.randomUUID().toString(), "student7", "General", "My Post 1", "Content A",
            java.time.LocalDateTime.now(), false));
		db.createPost(new Post(UUID.randomUUID().toString(), "student8", "General", "Other Post", "Content B",
            java.time.LocalDateTime.now(), false));

        // This should return posts belonging only to student7
		List<Post> myPosts = db.getMyPosts("student7");
		if (myPosts.size() == 2 && myPosts.get(0).getAuthor().equals("student7") && myPosts.get(1).getAuthor().equals("student7"))
			System.out.println("PASS");
		else
			System.out.println("FAIL (Did not return correct posts for user)");
	}

    /** 
     * Test 9 — Confirms that the system can fetch all posts (Student Story: “View All Posts”).  
     * Ensures global visibility works and that no data is hidden or lost.
     */
	private static void testAllPosts(Database db) {
		System.out.print("Test 9: Get All Posts → ");
		List<Post> allPosts = db.getAllPosts();
		if (allPosts.size() >= 2)
			System.out.println("PASS");
		else
			System.out.println("FAIL (Did not return all posts)");
	}
}
