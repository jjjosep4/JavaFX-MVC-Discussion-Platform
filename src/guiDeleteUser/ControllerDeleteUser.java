package guiDeleteUser;

import database.Database;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;

public class ControllerDeleteUser {
	/*-********************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	 */

	// Reference for the in-memory database so this package has access
    private static Database theDatabase = applicationMain.FoundationsMain.database;

    /**********
	 * <p> Method: doSelectUser() </p>
	 * 
	 * <p> Description: This method uses the ComboBox widget, fetches which item in the ComboBox
	 * was selected (a user in this case).</p>
	 * 
	 */
    protected static void doSelectUser() {
        ViewDeleteUser.theSelectedUser = (String) ViewDeleteUser.combobox_SelectUser.getValue();
    }

    /**********
	 * <p> Method: repaintTheWindow() </p>
	 * 
	 * <p> Description: This method determines the current state of the window and then establishes
	 * the appropriate list of widgets in the Pane to show the proper set of current values. </p>
	 * 
	 */
    protected static void repaintTheWindow() {
        ViewDeleteUser.theRootPane.getChildren().clear();

        if (ViewDeleteUser.theSelectedUser == null || ViewDeleteUser.theSelectedUser.isEmpty()) {
            ViewDeleteUser.theRootPane.getChildren().addAll(
                ViewDeleteUser.label_PageTitle,	ViewDeleteUser.label_UserDetails,
                ViewDeleteUser.line_Separator1,	ViewDeleteUser.label_SelectUser,
                ViewDeleteUser.combobox_SelectUser, ViewDeleteUser.line_Separator2,
                ViewDeleteUser.button_Return, ViewDeleteUser.button_Logout,
                ViewDeleteUser.button_Quit
            );
        } else {
            ViewDeleteUser.theRootPane.getChildren().addAll(
                ViewDeleteUser.label_PageTitle,	ViewDeleteUser.label_UserDetails,
                ViewDeleteUser.line_Separator1,	ViewDeleteUser.label_SelectUser,
                ViewDeleteUser.combobox_SelectUser, ViewDeleteUser.button_DeleteUser,
                ViewDeleteUser.line_Separator2,	ViewDeleteUser.button_Return,
                ViewDeleteUser.button_Logout, ViewDeleteUser.button_Quit
            );
        }

     // Set the title for the window
        ViewDeleteUser.theStage.setTitle("CSE 360 Foundation Code: Admin Delete User Page");
        ViewDeleteUser.theStage.setScene(ViewDeleteUser.theDeleteUserScene);
        ViewDeleteUser.theStage.show();
    }

    //Select and Delete a user
    protected static void performDeleteUser() {
        String selectedUser = ViewDeleteUser.theSelectedUser;

        if (selectedUser == null || selectedUser.isEmpty()) {
            showAlert("Error", "Select a user to delete.", Alert.AlertType.ERROR);
            return;
        }

        TextInputDialog confirmDialog = new TextInputDialog();
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete User: " + selectedUser + " Are you sure?");
        confirmDialog.setContentText("Type 'Yes' to confirm:");

        confirmDialog.showAndWait().ifPresent(response -> {
            if ("Yes".equals(response)) {
                if (theDatabase.deleteUser(selectedUser)) {
                    showAlert("Success", "User '" + selectedUser + "' deleted successfully.",
                            Alert.AlertType.INFORMATION);
                    ViewDeleteUser.combobox_SelectUser.getItems().remove(selectedUser);
                    ViewDeleteUser.combobox_SelectUser.getSelectionModel().clearSelection();
                    ViewDeleteUser.theSelectedUser = "";
                    repaintTheWindow();
                } else {
                    showAlert("Error", "Failed to delete user.", Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Cancelled", "Deletion not confirmed. Type 'Yes' to confirm.", 
                        Alert.AlertType.WARNING);
            }
        });
    }


    private static void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

	/**********
	 * <p> Method: performReturn() </p>
	 * 
	 * <p> Description: This method returns the user (who must be an Admin as only admins are the
	 * only users who have access to this page) to the Admin Home page. </p>
	 * 
	 */
    protected static void performReturn() {
        guiAdminHome.ViewAdminHome.displayAdminHome(ViewDeleteUser.theStage, ViewDeleteUser.theUser);
    }

	/**********
	 * <p> Method: performLogout() </p>
	 * 
	 * <p> Description: This method logs out the current user and proceeds to the normal login
	 * page where existing users can log in or potential new users with a invitation code can
	 * start the process of setting up an account. </p>
	 * 
	 */
    protected static void performLogout() {
        guiUserLogin.ViewUserLogin.displayUserLogin(ViewDeleteUser.theStage);
    }

    /**********
	 * <p> Method: performQuit() </p>
	 * 
	 * <p> Description: This method terminates the execution of the program.  It leaves the
	 * database in a state where the normal login page will be displayed when the application is
	 * restarted.</p>
	 * 
	 */
    protected static void performQuit() {
        System.exit(0);
    }
}

