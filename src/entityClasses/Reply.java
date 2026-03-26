package entityClasses;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/*******
 * <p> Title: Reply Class. </p>
 * 
 * <p> Description: Reply entity used by the discussion board. </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author
 * @version 1.00 2025-10-15
 */
public class Reply {

    private String replyID;
    private String postID;
    private String author;
    private String message;
    private LocalDateTime timestamp;
    private boolean isPrivate;

    public Reply() { }

    public Reply(String postID, String author, String message, boolean isPrivate) {
        this.replyID = UUID.randomUUID().toString();
        this.postID = postID;
        this.author = author;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.isPrivate = isPrivate;
    }

    public Reply(String replyID, String postID, String author, String message, LocalDateTime timestamp, boolean isPrivate) {
        this.replyID = replyID;
        this.postID = postID;
        this.author = author;
        this.message = message;
        this.timestamp = timestamp;
        this.isPrivate = isPrivate;
    }
    /* Getters / setters */
    public String getReplyID() { return replyID; }
    public String getPostID() { return postID; }
    public String getAuthor() { return author; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isPrivate() { return isPrivate; }

    public void setMessage(String m) { this.message = m; }
    public void setPrivate(boolean p) { this.isPrivate = p; }

    /* Helpers */
    public String getTimestampShort() {
        if (timestamp == null) return "";
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return timestamp.format(f);
    }

    @Override
    public String toString() {
        return author + ": " + message;
    }
}
