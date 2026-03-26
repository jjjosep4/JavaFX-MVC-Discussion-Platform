package guiStudentReply;

import java.time.LocalDateTime;

import applicationMain.FoundationsMain;
import database.Database;
import entityClasses.Reply;

/*******
 * <p> Title: ControllerStudentReply </p>
 * <p> Description: Controller for student reply actions. </p>
 */
public class ControllerStudentReply {

    private static Database db = FoundationsMain.database;

    public static void createReply(String postID) {
        if (postID == null) {
            ViewStudentReply.alertInfo.setTitle("No Post Selected");
            ViewStudentReply.alertInfo.setHeaderText(null);
            ViewStudentReply.alertInfo.setContentText("Select a post first.");
            ViewStudentReply.alertInfo.showAndWait();
            return;
        }
        String author = ViewStudentReply.getAuthorField();
        String message = ViewStudentReply.getMessageField();
        if (author == null || author.trim().isEmpty()) {
            ViewStudentReply.alertInfo.setTitle("Validation Error");
            ViewStudentReply.alertInfo.setHeaderText(null);
            ViewStudentReply.alertInfo.setContentText("Author is required.");
            ViewStudentReply.alertInfo.showAndWait();
            return;
        }
        if (message == null || message.trim().isEmpty()) {
            ViewStudentReply.alertInfo.setTitle("Validation Error");
            ViewStudentReply.alertInfo.setHeaderText(null);
            ViewStudentReply.alertInfo.setContentText("Message is required.");
            ViewStudentReply.alertInfo.showAndWait();
            return;
        }
        Reply r = new Reply(postID, author.trim(), message.trim(), false);
        boolean done = db.createReply(r);
        if (done) {
            ViewStudentReply.alertInfo.setTitle("Reply Added");
            ViewStudentReply.alertInfo.setHeaderText(null);
            ViewStudentReply.alertInfo.setContentText("Reply added.");
            ViewStudentReply.alertInfo.showAndWait();
            ViewStudentReply.clearFields();
            ViewStudentReply.refreshReplyList();
        } else {
            ViewStudentReply.alertInfo.setTitle("Error");
            ViewStudentReply.alertInfo.setHeaderText(null);
            ViewStudentReply.alertInfo.setContentText("Unable to add reply.");
            ViewStudentReply.alertInfo.showAndWait();
        }
    }

    public static void deleteSelectedReply() {
        String replyID = ViewStudentReply.getSelectedReplyID();
        if (replyID == null) {
            ViewStudentReply.alertInfo.setTitle("No Selection");
            ViewStudentReply.alertInfo.setHeaderText(null);
            ViewStudentReply.alertInfo.setContentText("Select a reply first.");
            ViewStudentReply.alertInfo.showAndWait();
            return;
        }
        boolean done = db.deleteReply(replyID);
        if (done) {
            ViewStudentReply.alertInfo.setTitle("Deleted");
            ViewStudentReply.alertInfo.setHeaderText(null);
            ViewStudentReply.alertInfo.setContentText("Reply deleted.");
            ViewStudentReply.alertInfo.showAndWait();
            ViewStudentReply.refreshReplyList();
        } else {
            ViewStudentReply.alertInfo.setTitle("Error");
            ViewStudentReply.alertInfo.setHeaderText(null);
            ViewStudentReply.alertInfo.setContentText("Unable to delete reply.");
            ViewStudentReply.alertInfo.showAndWait();
        }
    }
}
