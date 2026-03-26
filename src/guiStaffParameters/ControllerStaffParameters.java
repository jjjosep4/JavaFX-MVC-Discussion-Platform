package guiStaffParameters;

import java.util.List;

import applicationMain.FoundationsMain;
import database.Database;
import entityClasses.EvaluationParameter;
import javafx.stage.Stage;

/*******
 * <p> Title: ControllerStaffParameters Class. </p>
 *
 * <p> Description: Business logic for the Staff Parameter CRUD screen.
 * This controller performs validation, enforces rubric constraints, and
 * delegates all persistence work to the {@link Database}. It does NOT show
 * alerts or perform any GUI operations; instead, the view layer calls these
 * methods and displays any {@link IllegalArgumentException} messages to the
 * user. </p>
 *
 * <p> Structure and interfaces: </p>
 * <ul>
 *   <li>Uses a shared {@link Database} instance from
 *       {@link FoundationsMain#database} for all parameter operations.</li>
 *   <li>Exposes static methods for:
 *       <ul>
 *         <li>Retrieving all parameters ({@link #getAllParameters()})</li>
 *         <li>Creating a parameter ({@link #createParameter(String, String, String)})</li>
 *         <li>Updating a parameter ({@link #updateParameter(EvaluationParameter, String, String, String)})</li>
 *         <li>Deleting a parameter ({@link #deleteParameter(EvaluationParameter)})</li>
 *         <li>Basic navigation helpers (Return, Logout, Quit) invoked from the view</li>
 *       </ul>
 *   </li>
 *   <li>All validation failures and DB errors are reported via
 *       {@link IllegalArgumentException} so the view can display the messages
 *       directly in dialogs or status labels.</li>
 * </ul>
 *
 * <p> Business rules enforced: </p>
 * <ul>
 *   <li>Parameter name must be non-empty.</li>
 *   <li>Weight must be an integer in the inclusive range [1, 100].</li>
 *   <li>The sum of all parameter weights must not exceed 100. This is treated
 *       as a total “point budget” for the rubric.</li>
 * </ul>
 *
 * <p> Data used and produced: </p>
 * <ul>
 *   <li>Uses {@code evaluationParameterDB} (via {@link Database}) as the
 *       persistent store for parameters.</li>
 *   <li>Produces updated {@link EvaluationParameter} records in the database
 *       and updates in-memory objects so the UI stays in sync until the next
 *       refresh.</li>
 * </ul>
 *
 * <p> Validation and testing: The behavior of this controller is validated by
 * unit tests such as {@code ControllerStaffParametersTest} and database
 * integration tests that verify: </p>
 * <ul>
 *   <li>valid inputs create, update, and delete parameters as expected,</li>
 *   <li>invalid inputs result in appropriate {@link IllegalArgumentException}
 *       messages, and</li>
 *   <li>the total weight constraint (≤ 100) is enforced correctly for both
 *       create and update operations. </li>
 * </ul>
 */
public class ControllerStaffParameters {

    /** Convenience reference to the shared database instance used by all controllers. */
    private static final Database theDatabase = FoundationsMain.database;

    /*******
     * <p> Method: getAllParameters </p>
     *
     * <p> Description: Retrieve all evaluation parameters currently stored in
     * the database. The Staff Parameter GUI uses this list to populate its table
     * or list view. </p>
     *
     * <p> Data used and produced: </p>
     * <ul>
     *   <li>Uses {@link Database#getAllEvaluationParameters()} to load all
     *       {@link EvaluationParameter} records from {@code evaluationParameterDB}.</li>
     *   <li>Produces a {@link List} of {@link EvaluationParameter} objects for
     *       display and interaction in the view. </li>
     * </ul>
     *
     * @return list of {@link EvaluationParameter} currently stored
     */
    public static List<EvaluationParameter> getAllParameters() {
        return theDatabase.getAllEvaluationParameters();
    }

    /*******
     * <p> Method: createParameter </p>
     *
     * <p> Description: Create a new evaluation parameter after applying the
     * controller's validation rules. The view passes raw text from the UI
     * controls (name, description, and weight as text), and this method
     * performs trimming, parsing, and numeric checks before delegating to
     * {@link Database#createEvaluationParameter(String, String, int)}. </p>
     *
     * <p> Validation rules: </p>
     * <ul>
     *   <li>Name must not be empty.</li>
     *   <li>Weight must be an integer between 1 and 100, inclusive.</li>
     *   <li>The sum of all existing parameter weights plus the new weight
     *       must not exceed 100.</li>
     * </ul>
     *
     * <p> Why this approach: The validation logic is centralized here so that
     * all views or flows that may create parameters share the same rules.
     * Using a separate total-weight calculation against the database ensures
     * the constraint is enforced even if other parts of the system have added
     * parameters since the screen was last refreshed. </p>
     *
     * <p> Data used and produced: </p>
     * <ul>
     *   <li>Reads existing parameters from the database to compute the current
     *       total weight.</li>
     *   <li>On success, inserts a new row into {@code evaluationParameterDB}
     *       via the database helper method.</li>
     * </ul>
     *
     * <p> Validation and testing: Tests exercise this method with valid and
     * invalid inputs to confirm that correct exceptions are thrown and that
     * the 100-point total constraint is maintained. </p>
     *
     * @param name        parameter name from the text field
     * @param description parameter description from the text area
     * @param weightText  parameter weight as entered text
     *
     * @throws IllegalArgumentException if validation fails or DB insert fails
     */
    public static void createParameter(String name,
                                       String description,
                                       String weightText) {

        String trimmedName = (name == null) ? "" : name.trim();
        String trimmedDescription = (description == null) ? "" : description.trim();
        String trimmedWeight = (weightText == null) ? "" : weightText.trim();

        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }

        int weight;
        try {
            weight = Integer.parseInt(trimmedWeight);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Weight must be an integer between 1 and 100.");
        }

        if (weight < 1 || weight > 100) {
            throw new IllegalArgumentException("Weight must be between 1 and 100.");
        }

        // Compute the current total weight dynamically instead of caching it
        // in the GUI so that concurrent changes (e.g., from another staff
        // session) cannot accidentally violate the 100-point budget.
        int currentTotal = 0;
        for (EvaluationParameter p : theDatabase.getAllEvaluationParameters()) {
            currentTotal += p.getWeight();
        }

        if (currentTotal + weight > 100) {
            throw new IllegalArgumentException(
                "Creating this parameter would exceed the total allowed weight of 100.");
        }

        boolean ok = theDatabase.createEvaluationParameter(trimmedName, trimmedDescription, weight);
        if (!ok) {
            // We deliberately use a generic message here; the database may reject
            // the insert due to constraints such as a duplicate name or other
            // integrity rules that are not fully mirrored in the controller.
            throw new IllegalArgumentException("Unable to create parameter (possible duplicate name).");
        }
    }

    /*******
     * <p> Method: updateParameter </p>
     *
     * <p> Description: Update an existing evaluation parameter after applying
     * validation rules and checking the overall weight constraint. The view
     * supplies the currently selected {@link EvaluationParameter} and the new
     * values as text; this method parses and validates them, then calls
     * {@link Database#updateEvaluationParameter(int, String, String, int)}. </p>
     *
     * <p> Validation rules: </p>
     * <ul>
     *   <li>Target parameter must not be {@code null}.</li>
     *   <li>Name must not be empty.</li>
     *   <li>Weight must be an integer between 1 and 100, inclusive.</li>
     *   <li>The sum of all weights, substituting the new weight for the target
     *       parameter's old weight, must not exceed 100.</li>
     * </ul>
     *
     * <p> Why this approach: The method recomputes the total weight by
     * explicitly excluding the target parameter's existing weight and then
     * adding the new weight. This avoids off-by-one errors and ensures that
     * the constraint is checked as if the update had already taken place,
     * without actually writing to the database first. </p>
     *
     * <p> Data used and produced: </p>
     * <ul>
     *   <li>Reads all current parameters from the database to compute the
     *       total weight excluding the target.</li>
     *   <li>On success, updates the database row for the target parameter and
     *       then updates the in-memory {@link EvaluationParameter} so that the
     *       UI reflects the new values immediately.</li>
     * </ul>
     *
     * <p> Validation and testing: Tests verify that updating a parameter
     * respects the total weight constraint, correctly handles invalid weights
     * and names, and that the in-memory {@code target} is updated after a
     * successful database write. </p>
     *
     * @param target       the existing {@link EvaluationParameter} selected in the table
     * @param newName      new name from text field
     * @param newDesc      new description from text area
     * @param newWeightStr new weight as text from text field
     *
     * @throws IllegalArgumentException if validation fails or DB update fails
     */
    public static void updateParameter(EvaluationParameter target,
                                       String newName,
                                       String newDesc,
                                       String newWeightStr) {

        if (target == null) {
            throw new IllegalArgumentException("Please select a parameter to update.");
        }

        String trimmedName = (newName == null) ? "" : newName.trim();
        String trimmedDesc = (newDesc == null) ? "" : newDesc.trim();
        String trimmedWeight = (newWeightStr == null) ? "" : newWeightStr.trim();

        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }

        int newWeight;
        try {
            newWeight = Integer.parseInt(trimmedWeight);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Weight must be an integer between 1 and 100.");
        }

        if (newWeight < 1 || newWeight > 100) {
            throw new IllegalArgumentException("Weight must be between 1 and 100.");
        }

        // Compute weight total without this parameter, then add the new weight.
        // We compare names using equals() rather than object identity so that
        // parameters with the same logical name are handled correctly even if
        // they are represented by different String instances.
        int totalWithoutThis = 0;
        for (EvaluationParameter p : theDatabase.getAllEvaluationParameters()) {
            if (!p.getName().equals(target.getName())) {
                totalWithoutThis += p.getWeight();
            }
        }

        if (totalWithoutThis + newWeight > 100) {
            throw new IllegalArgumentException(
                "Updating this parameter would exceed the total allowed weight of 100.");
        }

        boolean ok = theDatabase.updateEvaluationParameter(
        	    target.getId(), trimmedName, trimmedDesc, newWeight);

        if (!ok) {
            throw new IllegalArgumentException("Unable to update parameter in the database.");
        }

        // Update in-memory object so the UI reflects new values immediately.
        // This avoids forcing an immediate full refresh from the database just
        // to show the new name/description/weight in the table.
        target.setName(trimmedName);
        target.setDescription(trimmedDesc);
        target.setWeight(newWeight);
    }

    /*******
     * <p> Method: deleteParameter </p>
     *
     * <p> Description: Delete an existing evaluation parameter from the
     * database after confirming that a target has been selected. </p>
     *
     * <p> Data used and produced: </p>
     * <ul>
     *   <li>Uses {@link EvaluationParameter#getId()} as the database primary key.</li>
     *   <li>Delegates the actual deletion to
     *       {@link Database#deleteEvaluationParameter(int)}.</li>
     * </ul>
     *
     * <p> Validation and testing: Tests confirm that a null target results in
     * an exception and that a successful delete removes the parameter from
     * the database. </p>
     *
     * @param target the parameter to delete
     *
     * @throws IllegalArgumentException if target is null or DB delete fails
     */
    public static void deleteParameter(EvaluationParameter target) {
        if (target == null) {
            throw new IllegalArgumentException("Please select a parameter to delete.");
        }

        boolean ok = theDatabase.deleteEvaluationParameter(target.getId());

        if (!ok) {
            throw new IllegalArgumentException("Unable to delete parameter from the database.");
        }
    }

    /**********
	 * <p> Method: performReturn() </p>
	 * 
	 * <p> Description: Return to the staff parameter view for the current user.
     * This method is invoked by the view when the "Return" action is selected. </p>
	 * 
	 * <p> Data used and produced: </p>
	 * <ul>
	 *   <li>Uses {@code ViewStaffParameters.theStage} and
	 *       {@code ViewStaffParameters.theUser} maintained by the view layer.</li>
	 *   <li>Produces a refreshed Staff Parameters GUI displayed on the same stage. </li>
	 * </ul>
	 */
	protected static void performReturn() {
		guiStaffParameters.ViewStaffParameters.displayStaffParameters(ViewStaffParameters.theStage,
				ViewStaffParameters.theUser);
	}
	
	
	/**********
	 * <p> Method: performLogout(Stage stage) </p>
	 * 
	 * <p> Description: Log out the current user and return to the normal login
	 * page where existing users can log in or potential new users with an
	 * invitation code can start setting up an account. </p>
	 *
     * <p> Why this approach: The implementation reuses
     * {@code ViewStaffParameters.theStage} rather than the {@code stage}
     * parameter to match the pattern used in other controllers and views in
     * FoundationsF25, where the active {@link Stage} is tracked centrally.
     * This reduces the risk of accidentally switching to or creating a
     * different stage. </p>
	 * 
	 * @param stage the current {@link Stage}; included for signature consistency
	 *              with other controllers but not used directly
	 */
	protected static void performLogout(Stage stage) {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewStaffParameters.theStage);
	}
	
	
	/**********
	 * <p> Method: performQuit() </p>
	 * 
	 * <p> Description: Terminate the execution of the program. When the
	 * application is restarted, it will display the normal login page. </p>
	 * 
	 * <p> Why this approach: Using {@code System.exit(0)} provides a simple and
	 * predictable shutdown for this single-user desktop application and keeps
	 * the quit behavior consistent across controllers. </p>
	 */
	protected static void performQuit() {
		System.exit(0);
	}
}
