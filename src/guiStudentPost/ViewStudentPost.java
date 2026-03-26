package guiStudentPost;

import applicationMain.FoundationsMain;


import database.Database;
import entityClasses.Post;
import entityClasses.Reply;
import entityClasses.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

/**
 * <p><b>ViewStudentPost Class</b></p>
 *
 * <p>This class is the View part of the MVC for the student discussion system.
 * It handles all the GUI layout and visual behavior for listing posts, showing details,
 * creating new posts, editing, deleting, and replying.  
 * The purpose is to let students perform CRUD actions through an interactive UI.</p>
 *
 * <p>Attributes correspond to UI elements (buttons, text boxes, labels, etc.).
 * These connect to the controller and model, which handle the data logic.  
 * The operations in this class directly reflect Student User Stories, such as:
 * <ul>
 *     <li>Viewing all posts or their own posts (Read)</li>
 *     <li>Creating a post or reply (Create)</li>
 *     <li>Editing or deleting their own posts (Update/Delete)</li>
 * </ul>
 * Each helper method or event binding supports one or more of those user interactions.</p>
 *
 * <p>Comments are written informally, but this class fully supports the required student CRUD behavior.</p>
 *
 * @version 2.0 - 2025-10-23
 */
public class ViewStudentPost {

    /** window width and height pulled from Foundations constants */
    private static double width = 1050;
    private static double height = 850;

    /** reference to current window and scene */
    protected static Stage theStage;
    private static Pane theRootPane;
    public static Scene theScene;

    /** reference to the current logged-in user */
    protected static User theUser;

    /** used to keep one instance of the view active */
    private static ViewStudentPost theView;

    /** link to the main shared database instance */
    private static Database db = FoundationsMain.database;

    /** list view of all posts displayed on the left side */
    private static ListView<Post> list_Posts = new ListView<>();

    /** UI labels and fields for showing post details */
    private static Label label_Title = new Label("Discussion Board");
    private static Label label_PostTitle = new Label();
    private static Label label_PostMeta = new Label();
    private static TextArea area_PostContent = new TextArea();
    private static ListView<String> list_Replies = new ListView<>();

    /** shared editor and related text fields/buttons used for create/edit/reply */
    private static Label label_Thread = new Label("Thread:");
    private static TextField txt_Thread = new TextField();
    private static Label label_NewTitle = new Label("Post Title:");
    private static TextField txt_NewTitle = new TextField();
    private static TextArea area_SharedEditor = new TextArea();
    private static Button btn_ShowAllPosts = new Button("All Posts");
    private static Button btn_ShowMyPosts = new Button("My Posts");
    private static Button btn_Submit = new Button("Submit");
    private static Button btn_Edit = new Button("Edit Post");
    private static Button btn_Delete = new Button("Delete Post");
    private static Button btn_Return = new Button("Return");
    private static Button btn_Logout = new Button("Logout");
    private static Button btn_Quit = new Button("Quit");
    private static TextField txt_Search = new TextField();
 // Staff-only thread controls (visible only for staff users)
    protected static Button button_CreateThread = new Button("Create Thread");
    protected static Button button_RenameThread = new Button("Rename Thread");
    protected static Button button_DeleteThread = new Button("Delete Thread");
    protected static Button button_PrivateFeedback = new Button("Send Private Feedback");


    /** holds whichever post the user clicked on */
    private static Post selectedPost = null;

    /** alert dialogs reused across actions */
    private static Alert info = new Alert(Alert.AlertType.INFORMATION);
    private static Alert error = new Alert(Alert.AlertType.INFORMATION);
    private static Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

    /**
     * Main entry to show the discussion board for the current student.  
     * Sets window size, builds UI, and populates posts.
     */
    public static void displayStudentPost(Stage stage, User user) {
        theStage = stage;
        theUser = user;

        if (theView == null) 
            theView = new ViewStudentPost();

        theStage.setWidth(width);
        theStage.setHeight(height);
        theStage.centerOnScreen();

        // ⭐ FIX: Re-apply visibility for current user's role EVERY time page opens.
        applyRoleVisibility();

        refreshPosts();
        theStage.setTitle("Discussion Board");
        theStage.setScene(theScene);
        theStage.show();
    }


    /**
     * Constructor builds all the UI elements for the discussion board screen.  
     * This includes layout setup, buttons, search bar, post list, and event handlers.
     * Everything here visually implements the CRUD actions described by the student user stories.
     */
    private ViewStudentPost() {
        theRootPane = new Pane();
        theScene = new Scene(theRootPane, width, height);

        // layout and UI setup
        setupLabelUI(label_Title, "Arial", 26, width, Pos.CENTER, 60, 0);

        /* search bar and search button trigger filtering of posts */
        setupTextUI(txt_Search, "Arial", 14, 300, Pos.BASELINE_LEFT, 20, 60, true);
        txt_Search.setPromptText("Search posts by keyword...");
        Button btn_Search = new Button("Search");
        setupButtonUI(btn_Search, "Dialog", 14, 120, Pos.CENTER, 340, 30);
        btn_Search.setOnAction(e -> {
            List<Post> results = Post.searchPosts(txt_Search.getText());
            setPostList(results);
        });

        /* post list on left, showing all or filtered posts */
        double leftWidth = width * 0.40;
        list_Posts.setLayoutX(20);
        list_Posts.setLayoutY(130);
        list_Posts.setPrefWidth(leftWidth - 30);
        list_Posts.setPrefHeight(350);

        list_Posts.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Post p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setText(null);
                } else {
                    String t = "[" + p.getThread() + "] " + p.getTitle();
                    String meta = " — by " + p.getAuthor();
                    setText(t + meta);
                    setStyle(p.isRead() ? "-fx-font-weight: normal;" : "-fx-font-weight: bold;");
                }
            }
        });

        /* listener: loads selected post content and replies */
        list_Posts.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            selectedPost = newV;
            if (selectedPost != null) selectedPost.setRead(true);
            loadSelectedPost();
        });

        /* Right pane positions (start after left pane + margin) */
        double rightX = leftWidth + 45;

        setupLabelUI(label_PostTitle, "Arial", 20, width - rightX, Pos.BASELINE_LEFT, rightX, 90);
        setupLabelUI(label_PostMeta, "Arial", 12, width - rightX, Pos.BASELINE_LEFT, rightX, 120);

        area_PostContent.setLayoutX(rightX);
        area_PostContent.setLayoutY(140);
        area_PostContent.setPrefWidth(width - rightX + 140);
        area_PostContent.setPrefHeight(160);
        area_PostContent.setWrapText(true);
        area_PostContent.setEditable(false);

        // Replies list
        Label lblReplies = new Label("Replies:");
        setupLabelUI(lblReplies, "Arial", 14, 200, Pos.BASELINE_LEFT, rightX, 310);
        list_Replies.setLayoutX(rightX);
        list_Replies.setLayoutY(330);
        list_Replies.setPrefWidth(width - rightX + 140);
        list_Replies.setPrefHeight(170);

        // Thread and Title text boxes
        setupLabelUI(label_Thread, "Arial", 14, 120, Pos.BASELINE_LEFT, 20, 515);
        setupTextUI(txt_Thread, "Arial", 14, 200, Pos.BASELINE_LEFT, 100, 510, true);
        txt_Thread.setPromptText("Enter thread (e.g., Assignment 1)");

        setupLabelUI(label_NewTitle, "Arial", 14, 120, Pos.BASELINE_LEFT, 320, 515);
        setupTextUI(txt_NewTitle, "Arial", 14, 300, Pos.BASELINE_LEFT, 410, 510, true);
        txt_NewTitle.setPromptText("Enter post subject");

        // Shared editor and helper text
        Label lblEditor = new Label("Shared editor (create post / edit / reply):");
        setupLabelUI(lblEditor, "Arial", 14, width, Pos.BASELINE_LEFT, 20, 545);

        area_SharedEditor.setLayoutX(20);
        area_SharedEditor.setLayoutY(570);
        area_SharedEditor.setPrefWidth(width + 120);
        area_SharedEditor.setPrefHeight(120);
        area_SharedEditor.setWrapText(true);
        area_SharedEditor.setPromptText("If no post selected: create a post. If a post is selected: reply or edit.");

        /* Buttons */
        setupButtonUI(btn_ShowAllPosts, "Dialog", 14, 150, Pos.CENTER, 480, 30);
        btn_ShowAllPosts.setOnAction(e -> refreshPosts());
        setupButtonUI(btn_ShowMyPosts, "Dialog", 14, 150, Pos.CENTER, 650, 30);
        btn_ShowMyPosts.setOnAction(e -> {
        	List<Post> myPosts = Post.getMyPosts(theUser.getUserName());
        	setPostList(myPosts);
        });
        
        /* Staff-only thread control buttons (threads == post threads) */
        setupButtonUI(button_CreateThread, "Dialog", 14, 120, Pos.CENTER, 340, 60);
        setupButtonUI(button_RenameThread, "Dialog", 14, 150, Pos.CENTER, 480, 60);
        setupButtonUI(button_DeleteThread, "Dialog", 14, 150, Pos.CENTER, 650, 60);

    

        // Create Thread → just preps a thread name for the next post
        button_CreateThread.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Create Thread");
            dialog.setHeaderText("Create a new discussion thread");
            dialog.setContentText("Thread name:");

            Optional<String> result = dialog.showAndWait();
            if (!result.isPresent()) return;

            String name = result.get().trim();
            if (name.isEmpty()) {
                error("Invalid Name", "Thread name cannot be empty.");
                return;
            }
        

            // For now, threads are just names used in posts. We pre-fill the thread field
            // so when staff creates a post, that post will define the thread.
            txt_Thread.setText(name);
            info("Thread Ready", "Thread \"" + name + "\" will exist once a post is created with it.");
        });

        // Rename Thread → uses Database.renameThread on all posts with that thread
        button_RenameThread.setOnAction(e -> {
            List<String> threads = db.getAllThreadNames();
            if (threads == null || threads.isEmpty()) {
                info("No Threads", "There are no existing threads to rename.");
                return;
            }

            TextInputDialog oldDialog = new TextInputDialog();
            oldDialog.setTitle("Rename Thread");
            oldDialog.setHeaderText("Existing threads:\n" + String.join(", ", threads));
            oldDialog.setContentText("Thread to rename:");
            Optional<String> oldRes = oldDialog.showAndWait();
            if (!oldRes.isPresent()) return;

            String oldName = oldRes.get().trim();
            if (oldName.isEmpty()) {
                error("Invalid Name", "Thread name cannot be empty.");
                return;
            }

            TextInputDialog newDialog = new TextInputDialog();
            newDialog.setTitle("Rename Thread");
            newDialog.setHeaderText("Renaming thread: " + oldName);
            newDialog.setContentText("New name:");
            Optional<String> newRes = newDialog.showAndWait();
            if (!newRes.isPresent()) return;

            String newName = newRes.get().trim();
            if (newName.isEmpty()) {
                error("Invalid Name", "New thread name cannot be empty.");
                return;
            }

            boolean ok = db.renameThread(oldName, newName);
            if (ok) {
                info("Thread Renamed", "Thread \"" + oldName + "\" renamed to \"" + newName + "\".");
                refreshPosts();
            } else {
                error("No Changes", "No posts were found for thread \"" + oldName + "\".");
            }
        });

        // Delete Thread → uses Database.deleteThread to soft-delete all posts in that thread
        button_DeleteThread.setOnAction(e -> {
            List<String> threads = db.getAllThreadNames();
            if (threads == null || threads.isEmpty()) {
                info("No Threads", "There are no existing threads to delete.");
                return;
            }

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Delete Thread");
            dialog.setHeaderText("Existing threads:\n" + String.join(", ", threads));
            dialog.setContentText("Thread to delete (all posts will be deleted):");
            Optional<String> res = dialog.showAndWait();
            if (!res.isPresent()) return;

            String name = res.get().trim();
            if (name.isEmpty()) {
                error("Invalid Name", "Thread name cannot be empty.");
                return;
            }

            confirm.setTitle("Confirm Delete Thread");
            confirm.setHeaderText(null);
            confirm.setContentText("Delete all posts in thread \"" + name + "\"?");
            var opt = confirm.showAndWait();
            if (!opt.isPresent() || opt.get() != ButtonType.OK) return;

            boolean ok = db.deleteThread(name);
            if (ok) {
                info("Thread Deleted", "All posts for thread \"" + name + "\" were deleted.");
                refreshPosts();
            } else {
                error("No Changes", "No posts were found for thread \"" + name + "\".");
            }
        });
        
     // ---------- PRIVATE FEEDBACK (STAFF ONLY) ----------
        setupButtonUI(button_PrivateFeedback, "Dialog", 14, 130, Pos.CENTER, 820, 60);
        button_PrivateFeedback.setOnAction(e -> {
            if (selectedPost == null) {
                error("No Post Selected", "Select a student post before leaving private feedback.");
                return;
            }

            // staff only
            if (!theUser.getRole2()) {
                error("Not Allowed", "Only staff can leave private feedback.");
                return;
            }

            String message = area_SharedEditor.getText().trim();
            if (message.isEmpty()) {
                error("Empty Feedback", "Enter feedback in the editor before submitting.");
                return;
            }

            boolean ok = Post.createReply(
                    selectedPost.getPostID(),
                    theUser.getUserName(),
                    "[PRIVATE] " + message,
                    true    // <── isPrivate = true
            );

            if (ok) {
                info("Private Feedback Added", "The student will see this private feedback on their post.");
                area_SharedEditor.clear();
                loadSelectedPost();        // refresh only right side
            } else {
                error("Error", "Unable to submit private feedback.");
            }
        });


        setupButtonUI(btn_Submit, "Dialog", 14, 120, Pos.CENTER, 20, 720);
        btn_Submit.setOnAction(e -> {
            String text = area_SharedEditor.getText();
            if (text == null || text.trim().isEmpty()) {
                info("Empty", "Enter text before submitting.");
                return;
            }
            String currentUser = Post.getCurrentUsername();
            if (selectedPost == null) {
                String thread = txt_Thread.getText().isBlank() ? "General" : txt_Thread.getText().trim();
                String title = txt_NewTitle.getText().isBlank() ? "(untitled)" : txt_NewTitle.getText().trim();
                boolean done = Post.createPost(currentUser, thread, title, text);
                if (done) {
                    info("Created", "Post created.");
                    area_SharedEditor.clear();
                    txt_NewTitle.clear();
                    txt_Thread.clear();
                    refreshPosts();
                } else error("Error", "Unable to create post.");
            } else {
                if (selectedPost.getAuthor().equals(currentUser)) {
                    boolean done = Post.updatePost(selectedPost.getPostID(), selectedPost.getTitle(), text);
                    if (done) { info("Updated", "Post updated."); area_SharedEditor.clear(); refreshPosts(); }
                    else error("Error", "Unable to update post.");
                } else {
                    boolean done = Post.createReply(selectedPost.getPostID(), currentUser, text, false);
                    if (done) { info("Reply", "Reply added."); area_SharedEditor.clear(); loadSelectedPost(); }
                    else error("Error", "Unable to add reply.");
                }
            }
        });

        setupButtonUI(btn_Edit, "Dialog", 14, 120, Pos.CENTER, 180, 720);
        btn_Edit.setOnAction(e -> {
            if (selectedPost == null) {
                info("No post selected", "Select a post to edit.");
                return;
            }
            String current = Post.getCurrentUsername();
            if (!selectedPost.getAuthor().equals(current)) {
                error("Permission denied", "You may only edit your own posts.");
                return;
            }
            area_SharedEditor.setText(selectedPost.getContent());
            area_SharedEditor.requestFocus();
        });

        setupButtonUI(btn_Delete, "Dialog", 14, 120, Pos.CENTER, 340, 720);
        btn_Delete.setOnAction(e -> {
            if (selectedPost == null) { info("No post selected", "Select a post to delete."); return; }
            String current = Post.getCurrentUsername();
            boolean isStaff = theUser.getRole2();   // staff role flag
            // Students can delete only their own posts.
            // Staff can delete ANY post.
            if (!isStaff && !selectedPost.getAuthor().equals(current)) {
                error("Permission denied", "You may only delete your own posts.");
                return;
            }
            confirm.setTitle("Confirm Delete"); confirm.setHeaderText(null); confirm.setContentText("Are you sure?");
            var opt = confirm.showAndWait();
            if (opt.isPresent() && opt.get() == ButtonType.OK) {
                boolean ok = Post.deletePost(selectedPost.getPostID());
                if (ok) { info("Deleted", "Post deleted."); refreshPosts(); }
                else error("Error", "Unable to delete post.");
            }
        });
        
        

        setupButtonUI(btn_Return, "Dialog", 14, 120, Pos.CENTER, 500, 720);
        btn_Return.setOnAction(e -> {
            theStage.setWidth(820);
            theStage.setHeight(625);
            theStage.centerOnScreen();

            if (theUser.getRole2()) {
                // STAFF
                guiRoleStaff.ViewRoleStaffHome.displayRoleStaffHome(theStage, theUser);
            } else {
                // STUDENT
                ControllerStudentPost.returnToHome(theStage);
            }
        });



        setupButtonUI(btn_Logout, "Dialog", 14, 120, Pos.CENTER, 660, 720);
        btn_Logout.setOnAction(e -> ControllerStudentPost.performLogout(theStage));

        setupButtonUI(btn_Quit, "Dialog", 14, 120, Pos.CENTER, 820, 720);
        btn_Quit.setOnAction(e -> ControllerStudentPost.performQuit());

        /* Add nodes */
        theRootPane.getChildren().addAll(
                label_Title, txt_Search, btn_Search,
                list_Posts,
                label_PostTitle, label_PostMeta, area_PostContent,
                lblReplies, list_Replies,
                label_Thread, txt_Thread, label_NewTitle, txt_NewTitle,
                lblEditor, area_SharedEditor,
                button_CreateThread, button_RenameThread, button_DeleteThread, button_PrivateFeedback,
                btn_ShowAllPosts, btn_ShowMyPosts, btn_Submit, btn_Edit, btn_Delete, btn_Return, btn_Logout, btn_Quit
        );
    }
    

    private static void applyRoleVisibility() {
        // First hide everything
        button_CreateThread.setVisible(false);
        button_RenameThread.setVisible(false);
        button_DeleteThread.setVisible(false);
        button_PrivateFeedback.setVisible(false);

        // Now apply rules for CURRENT user
        if (theUser != null && theUser.getRole2()) {    // STAFF role == role2
            button_CreateThread.setVisible(true);
            button_RenameThread.setVisible(true);
            button_DeleteThread.setVisible(true);
            button_PrivateFeedback.setVisible(true);
        }
    }
    
    /** Reloads post list from database (Read part of CRUD). */
    private static void refreshPosts() {
        List<Post> posts = Post.getAllPosts();
        setPostList(posts);
    }

    /** Helper to set new post list items into the ListView. */
    private static void setPostList(List<Post> posts) {
        ObservableList<Post> items = FXCollections.observableArrayList(posts);
        list_Posts.setItems(items);
        if (items.isEmpty()) {
            selectedPost = null;
            clearDetail();
        }
    }

    /** Clears all displayed post/reply details when nothing is selected. */
    private static void clearDetail() {
        label_PostTitle.setText("");
        label_PostMeta.setText("");
        area_PostContent.setText("");
        list_Replies.getItems().clear();
    }

    /**
     * Loads the selected post and its replies into the right pane.  
     * Implements the “Read Post” user story requirement.
     */
    private static void loadSelectedPost() {
        if (selectedPost == null) {
            clearDetail();
            return;
        }

        label_PostTitle.setText(selectedPost.getTitle() + (selectedPost.isDeleted() ? " (deleted)" : ""));
        label_PostMeta.setText("by " + selectedPost.getAuthor() + " | " +
                               selectedPost.getTimestampShort() + " | Thread: " +
                               selectedPost.getThread());
        area_PostContent.setText(selectedPost.getContent());

        List<Reply> replies = Post.getRepliesForPost(selectedPost.getPostID());
        ObservableList<String> repItems = FXCollections.observableArrayList();

        String currentUsername = theUser.getUserName();

        for (Reply r : replies) {

            // PRIVATE FEEDBACK FILTER:
            if (r.isPrivate()) {
                boolean isStaff = theUser.getRole2(); // staff role = 3
                boolean isOwnerOfPost = selectedPost.getAuthor().equals(currentUsername);
                boolean isAuthorOfReply = r.getAuthor().equals(currentUsername);

                // Only show if:
                // - staff
                // - student whose post is being commented on
                // - author of the private reply (staff viewing own feedback)
                if (!isStaff && !isOwnerOfPost && !isAuthorOfReply) {
                    continue; // Hide private feedback from other students
                }
            }

            // Display reply normally
            repItems.add(r.getAuthor() + " (" + r.getTimestampShort() + "): " + r.getMessage());
        }

        list_Replies.setItems(repItems);
    }


    /** Simple helper to set up label visuals quickly. */
    private void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, double y){
        l.setFont(Font.font(ff, f));
        l.setMinWidth(w);
        l.setAlignment(p);
        l.setLayoutX(x);
        l.setLayoutY(y);
    }

    /** Simple helper to style and position buttons. */
    private void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y){
        b.setFont(Font.font(ff, f));
        b.setMinWidth(w);
        b.setAlignment(p);
        b.setLayoutX(x);
        b.setLayoutY(y);
    }

    /** Simple helper to style text fields quickly. */
    private void setupTextUI(TextField t, String ff, double f, double w, Pos p, double x, double y, boolean e){
        t.setFont(Font.font(ff, f));
        t.setMinWidth(w);
        t.setMaxWidth(w);
        t.setAlignment(p);
        t.setLayoutX(x);
        t.setLayoutY(y);
        t.setEditable(e);
    }

    /** Quick helper to show informational alerts. */
    private static void info(String title, String message) {
        info.setTitle(title); info.setHeaderText(null); info.setContentText(message); info.showAndWait();
    }

    /** Quick helper to show error alerts. */
    private static void error(String title, String message) {
        error.setTitle(title); error.setHeaderText(null); error.setContentText(message); error.showAndWait();
    }
}
