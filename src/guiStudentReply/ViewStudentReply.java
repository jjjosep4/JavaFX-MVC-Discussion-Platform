package guiStudentReply;

import java.util.List;
import applicationMain.FoundationsMain;
import database.Database;
import entityClasses.Reply;
import entityClasses.Post;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/*******
 * <p> Title: ViewStudentReply </p>
 * <p> Description: GUI for viewing and creating replies for a selected post. </p>
 */
public class ViewStudentReply {

    private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
    private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

    protected static Label label_Title = new Label("Replies");
    protected static ListView<String> list_Replies = new ListView<>();
    protected static Button btn_Add = new Button("Add Reply");
    protected static Button btn_Delete = new Button("Delete Reply");
    protected static Button btn_Back = new Button("Back");

    protected static TextField txt_Author = new TextField();
    protected static TextArea txt_Message = new TextArea();

    protected static Alert alertInfo = new Alert(Alert.AlertType.INFORMATION);
    protected static Alert alertError = new Alert(Alert.AlertType.INFORMATION);

    private static Stage theStage;
    private static ViewStudentReply theView = null;
    private static Pane theRoot;
    public static Scene theScene;

    private static Database theDatabase = applicationMain.FoundationsMain.database;

    private static String currentPostID = null;

    public static void setCurrentPostID(String postID) { currentPostID = postID; }

    public static void displayStudentReply(Stage stage) {
        theStage = stage;
        if (theView == null) theView = new ViewStudentReply();
        refreshReplyList();
        theStage.setTitle("Replies - Post");
        theStage.setScene(theScene);
        theStage.show();
    }

    private ViewStudentReply() {
        theRoot = new Pane();
        theScene = new Scene(theRoot, width, height);

        setupLabelUI(label_Title, "Arial", 26, width, Pos.CENTER, 0, 5);

        list_Replies.setLayoutX(20); list_Replies.setLayoutY(50);
        list_Replies.setPrefWidth(480); list_Replies.setPrefHeight(420);

        setupLabelUI(new Label("Author:"), "Arial", 14, 80, Pos.BASELINE_LEFT, 520, 60);
        setupTextUI(txt_Author, "Arial", 14, 250, Pos.BASELINE_LEFT, 520, 90, true);

        setupLabelUI(new Label("Message:"), "Arial", 14, 80, Pos.BASELINE_LEFT, 520, 140);
        txt_Message.setLayoutX(520); txt_Message.setLayoutY(170);
        txt_Message.setPrefWidth(250); txt_Message.setPrefHeight(200);
        txt_Message.setWrapText(true);

        setupButtonUI(btn_Add, "Dialog", 14, 150, Pos.CENTER, 520, 390);
        btn_Add.setOnAction(e -> ControllerStudentReply.createReply(currentPostID));

        setupButtonUI(btn_Delete, "Dialog", 14, 150, Pos.CENTER, 520, 430);
        btn_Delete.setOnAction(e -> ControllerStudentReply.deleteSelectedReply());

        setupButtonUI(btn_Back, "Dialog", 14, 150, Pos.CENTER, 520, 470);
        btn_Back.setOnAction(e -> {
            // go back to posts screen          
            guiStudentPost.ViewStudentPost.displayStudentPost(theStage, null);
        });

        theRoot.getChildren().addAll(label_Title, list_Replies, txt_Author, txt_Message, btn_Add, btn_Delete, btn_Back);
    }

    public static void refreshReplyList() {
        list_Replies.getItems().clear();
        if (currentPostID == null) return;
        List<Reply> replies = theDatabase.getRepliesForPost(currentPostID);
        for (Reply r : replies) {
            String disp = r.getAuthor() + ": " + r.getMessage() + " | id:" + r.getReplyID();
            list_Replies.getItems().add(disp);
        }
    }

    public static String getSelectedReplyID() {
        String sel = list_Replies.getSelectionModel().getSelectedItem();
        if (sel == null) return null;
        int i = sel.indexOf("| id:");
        if (i >= 0) return sel.substring(i + 5).trim();
        return sel;
    }

    public static String getAuthorField() { return txt_Author.getText(); }
    public static String getMessageField() { return txt_Message.getText(); }

    public static void clearFields() { txt_Author.setText(""); txt_Message.setText(""); }

    // UI helpers
    private void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, double y){
        l.setFont(Font.font(ff,f));
        l.setMinWidth(w);
        l.setAlignment(p);
        l.setLayoutX(x);
        l.setLayoutY(y);
    }
    private void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y){
        b.setFont(Font.font(ff,f));
        b.setMinWidth(w);
        b.setAlignment(p);
        b.setLayoutX(x);
        b.setLayoutY(y);
    }
    private void setupTextUI(TextField t, String ff, double f, double w, Pos p, double x, double y, boolean e){
        t.setFont(Font.font(ff,f));
        t.setMinWidth(w);
        t.setMaxWidth(w);
        t.setAlignment(p);
        t.setLayoutX(x);
        t.setLayoutY(y);
        t.setEditable(e);
    }
}
