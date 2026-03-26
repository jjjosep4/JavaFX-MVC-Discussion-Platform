package entityClasses;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import applicationMain.FoundationsMain;
import database.Database;

/**
 * <p><b>Post Class</b></p>
 *
 * <p>This class represents a post in the student discussion system.  
 * It covers all the CRUD (Create, Read, Update, Delete) operations 
 * plus some simple validation.  Students can create, edit, or delete 
 * their posts, and later staff can review or leave feedback (for TP3).</p>
 *
 * <p>The goal is to keep this class simple and consistent with the
 * Foundations code, but still show the main logic for CRUD and input 
 * checking.  These comments explain what each part is for in a quick, 
 * informal style.</p>
 *
 * @author Team 30
 * 
 * @version 2.0 - 2025-10-23
 */
public class Post {

    // attributes 

    /** unique ID for this post */
    private String postID;

    /** username of whoever made the post */
    private String authorUsername;

    /** display name of the author */
    private String author;

    /** which thread this post belongs to (default General) */
    private String thread;

    /** post title text */
    private String title;

    /** main message text */
    private String content;

    /** timestamp for when post was created */
    private LocalDateTime timestamp;

    /** true if post marked as deleted */
    private boolean deleted;

    /** used for tracking read/unread in the GUI */
    private boolean isRead;

    
    // Staff Epic attributes for TP3 
    
    /** feedback text left by staff (TP3 use) */
    private String feedback;

    /** who left the feedback (TP3 use) */
    private String feedbackAuthor;

    /** when the feedback was left (TP3 use) */
    private LocalDateTime feedbackTimestamp;

    /** status of an admin/staff request related to this post (TP3 use) */
    private String adminRequestStatus;

    /** ID of the admin/staff request (TP3 use) */
    private String adminRequestID;


    
    //  constructors 

    /** simple empty constructor */
    public Post() { }

    /**
     * constructor for creating a new post  
     * this is used by students when making a new post
     */
    public Post(String author, String thread, String title, String content) {
        this.postID = UUID.randomUUID().toString();
        this.author = author;
        this.thread = thread == null ? "General" : thread;
        this.title = title;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.deleted = false;


        // Initialization for staff epics 
 
        this.feedback = "";
        this.feedbackAuthor = "";
        this.feedbackTimestamp = null;
        this.adminRequestStatus = "None";
        this.adminRequestID = "";
    }

    /**
     * constructor for loading posts already stored in DB
     */
    public Post(String id, String author, String thread, String title, String content, LocalDateTime timestamp, boolean deleted) {
        this.postID = id;
        this.author = author;
        this.thread = (thread == null || thread.trim().isEmpty()) ? "General" : thread.trim();
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.deleted = deleted;
    }


    
    //  basic getters/setters 


    /** @return post ID */
    public String getPostID() { return postID; }

    /** @return author name */
    public String getAuthor() { return author; }

    /** @return thread name */
    public String getThread() { return thread; }

    /** @return title text */
    public String getTitle() { return title; }

    /** @return post content */
    public String getContent() { return content; }

    /** @return when post was made */
    public LocalDateTime getTimestamp() { return timestamp; }

    /** @return true if deleted */
    public boolean isDeleted() { return deleted; }

    /** @return true if read by user */
    public boolean isRead() { return isRead; }

    /** change post title */
    public void setTitle(String s) { this.title = s; }

    /** change post content */
    public void setContent(String s) { this.content = s; }
    
    /** change thread */
    public void setThread(String s) { this.content = s; }

    /** soft delete toggle */
    public void setDeleted(boolean d) { this.deleted = d; }

    /** mark read/unread */
    public void setRead(boolean r) { this.isRead = r; }


    
    //  Staff Epic getters/setters 

    /** @return staff feedback text */
    public String getFeedback() { return feedback; }

    /** @return who left feedback */
    public String getFeedbackAuthor() { return feedbackAuthor; }

    /** @return feedback timestamp */
    public LocalDateTime getFeedbackTimestamp() { return feedbackTimestamp; }

    /** @return admin/staff request status */
    public String getAdminRequestStatus() { return adminRequestStatus; }

    /** @return admin/staff request ID */
    public String getAdminRequestID() { return adminRequestID; }

    /** set new feedback */
    public void setFeedback(String feedback, String author) {
        this.feedback = feedback;
        this.feedbackAuthor = author;
        this.feedbackTimestamp = LocalDateTime.now();
    }

    /** set admin request info */
    public void setAdminRequest(String status, String id) {
        this.adminRequestStatus = status;
        this.adminRequestID = id;
    }


    
    //  small helpers 

    /** make timestamp short and neat */
    public String getTimestampShort() {
        if (timestamp == null) return "";
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return timestamp.format(f);
    }

    /** quick summary text version */
    @Override
    public String toString() {
        return "[" + thread + "] " + title + " — by " + author;
    }

    
    //  ***CRUD operations (through database) ***

    /** global database connection from Foundations */
    private static Database db = FoundationsMain.database;

    /** read all posts (used to show list) */
    public static List<Post> getAllPosts() {
        return db.getAllPosts();
    }

    /** read posts by keyword */
    public static List<Post> searchPosts(String keyword) {
        return db.searchPostsByKeyword(keyword);
    }

    /** read posts made by one author */
    public static List<Post> getMyPosts(String author) {
        return db.getMyPosts(author);
    }

    /**
     * Create new post and push to DB
     * (basic validation included)
     */
    public static boolean createPost(String author, String thread, String title, String content) {
        if (author == null || title == null || title.trim().isEmpty()) return false; // validation
        Post p = new Post(author, thread == null ? "General" : thread, title.trim(), content == null ? "" : content.trim());
        return db.createPost(p);
    }

    /** update an existing post */
    public static boolean updatePost(String postID, String newTitle, String newContent) {
        return db.updatePost(postID, newTitle, newContent);
    }

    /** delete (soft) post */
    public static boolean deletePost(String postID) {
        return db.softDeletePost(postID);
    }

    /** get post by ID */
    public static Post getPostById(String postID) {
        return db.getPostById(postID);
    }

    /** add a reply under post */
    public static boolean createReply(String postID, String author, String message, boolean isPrivate) {
        if (postID == null || message == null || message.trim().isEmpty()) return false;
        Reply r = new Reply(postID, author == null ? "anonymous" : author, message.trim(), isPrivate);
        return db.createReply(r);
    }

    /** get all replies for post */
    public static List<Reply> getRepliesForPost(String postID) {
        return db.getRepliesForPost(postID);
    }

    /** delete a reply */
    public static boolean deleteReply(String replyID) {
        return db.deleteReply(replyID);
    }

    /** edit reply */
    public static boolean updateReply(String replyID, String newMessage) {
        return db.updateReply(replyID, newMessage);
    }

    /** @return current logged-in username */
    public static String getCurrentUsername() {
        return db.getCurrentUsername();
    }
}
