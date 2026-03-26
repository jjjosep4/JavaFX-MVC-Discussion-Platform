package guiStaffEvaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import applicationMain.FoundationsMain;
import database.Database;
import entityClasses.EvaluationParameter;
import entityClasses.User;
import guiStaffParameters.ViewStaffParameters;
import javafx.stage.Stage;

/*******
 * <p> Title: ControllerStaffEvaluation Class. </p>
 * 
 * <p> Description: Controller logic that supports staff evaluation of students.
 * This class provides helper methods for the staff evaluation view to:
 * </p>
 * 
 * <ul>
 *   <li>Retrieve the list of students available for evaluation.</li>
 *   <li>Retrieve the evaluation parameters defined by staff.</li>
 *   <li>Validate and persist evaluation scores entered by staff.</li>
 *   <li>Handle navigation actions (Return, Logout, Quit) from the evaluation view.</li>
 * </ul>
 * 
 * <p> Structure and interfaces: </p>
 * <ul>
 *   <li>All methods are {@code public static} or {@code protected static},
 *       consistent with other controller classes in the FoundationsF25
 *       application (e.g., student and admin controllers).</li>
 *   <li>The class uses a shared {@link Database} instance obtained from
 *       {@link FoundationsMain#database} so that all controllers operate on the
 *       same underlying data store.</li>
 *   <li>The {@link #saveEvaluation(User, Map, String)} method encapsulates
 *       validation and data persistence for evaluations, throwing an
 *       {@link IllegalArgumentException} when validation fails so that the
 *       view can display the error message directly to the user.</li>
 * </ul>
 * 
 * <p> Data used and produced: </p>
 * <ul>
 *   <li>Uses user records and evaluation parameters from the database to
 *       populate the evaluation user interface.</li>
 *   <li>Produces evaluation records (one per parameter per student) stored
 *       in {@code evaluationRecordDB} via the database helper
 *       {@code createEvaluationRecord}.</li>
 *   <li>Navigation methods produce transitions back to the appropriate views
 *       (login screen or evaluation screen) and can terminate the application
 *       in the case of Quit.</li>
 * </ul>
 * 
 * <p> Validation and testing: The behavior of this controller is validated by
 * the JUnit tests for the staff evaluation feature (for example,
 * {@code ControllerStaffEvaluationTest} and related integration tests), which
 * verify that:
 * </p>
 * <ul>
 *   <li>students and parameters are fetched correctly from the database,</li>
 *   <li>validation logic in {@code saveEvaluation} rejects invalid scores and
 *       aggregates error messages appropriately, and</li>
 *   <li>successful evaluations result in the expected calls to
 *       {@code createEvaluationRecord} for each parameter.</li>
 * </ul>
 */
public class ControllerStaffEvaluation {

    private static final Database theDatabase = FoundationsMain.database;

    /*******
     * <p> Method: getAllUsersForEvaluation </p>
     * 
     * <p> Description: Return the list of students available for evaluation.
     * This method delegates to the database helper that selects users based on
     * the "student" role flag. The view may perform additional filtering if
     * needed (for example, by section or group). </p>
     * 
     * <p> Data used and produced: </p>
     * <ul>
     *   <li>Uses {@link Database#getAllStudents()} to retrieve the users
     *       marked as students.</li>
     *   <li>Produces a {@link List} of {@link User} objects that the view can
     *       display for selection. </li>
     * </ul>
     * 
     * <p> Validation and testing: Covered by staff-evaluation tests that
     * confirm the list contains only student-role users and that it remains
     * consistent with the underlying database contents. </p>
     * 
     * @return list of all student {@link User} objects
     */
    public static List<User> getAllUsersForEvaluation() {
        // We call the role-specific accessor here rather than a generic "getAllUsers"
        // so that the evaluation UI is automatically limited to students without
        // requiring the view to implement its own role filtering logic.
        return theDatabase.getAllStudents();
    }

    /*******
     * <p> Method: getAllParameters </p>
     * 
     * <p> Description: Return all evaluation parameters defined by staff in
     * the database. These parameters form the rubric used to evaluate each
     * student's discussion performance. </p>
     * 
     * <p> Data used and produced: </p>
     * <ul>
     *   <li>Uses {@link Database#getAllEvaluationParameters()} to load all
     *       parameter records from {@code evaluationParameterDB}.</li>
     *   <li>Produces a {@link List} of {@link EvaluationParameter} objects
     *       that the view uses to build the evaluation UI (one input per
     *       parameter). </li>
     * </ul>
     * 
     * <p> Validation and testing: Staff-evaluation tests verify that the
     * list returned here matches the parameters configured in the staff
     * parameter management GUI. </p>
     * 
     * @return list of {@link EvaluationParameter} instances
     */
    public static List<EvaluationParameter> getAllParameters() {
        return theDatabase.getAllEvaluationParameters();
    }

    /*******
     * <p> Method: saveEvaluation </p>
     * 
     * <p> Description: Validate and persist the evaluation scores for a given
     * student. The caller supplies a map from {@link EvaluationParameter} to
     * integer score and the username of the staff evaluator. If any validation
     * fails (missing student, missing scores, or out-of-range scores), this
     * method throws an {@link IllegalArgumentException} containing one or more
     * human-readable error messages that the view can display. </p>
     * 
     * <p> Actions performed: </p>
     * <ul>
     *   <li>Checks that a student has been selected.</li>
     *   <li>Checks that at least one parameter is present.</li>
     *   <li>For each parameter/score:
     *     <ul>
     *       <li>verifies a score was entered, and</li>
     *       <li>enforces that each score is between {@code 0} and
     *           the parameter's {@code weight} (inclusive).</li>
     *     </ul>
     *   </li>
     *   <li>If validation passes, inserts one evaluation record per parameter
     *       using {@link Database#createEvaluationRecord(String, String, int, int, String)}.</li>
     * </ul>
     * 
     * <p> Why this approach: Rather than stopping at the first error, the method
     * collects all validation problems into a list and reports them together.
     * This reduces the number of "fix one field, resubmit, see another error"
     * cycles for the user and provides a better overall UX. Using the parameter's
     * {@code weight} as the upper bound for the score keeps the rubric consistent:
     * a score can never exceed the maximum points assigned to that parameter. </p>
     * 
     * <p> Data used and produced: </p>
     * <ul>
     *   <li>Uses the {@link User} object to determine the student's username.</li>
     *   <li>Uses the parameter names and weights from each {@link EvaluationParameter}
     *       as metadata for validation and persistence.</li>
     *   <li>Produces rows in {@code evaluationRecordDB}, or throws an exception
     *       if validation or persistence fails. </li>
     * </ul>
     * 
     * <p> Validation and testing: JUnit tests for {@code ControllerStaffEvaluation}
     * and integration tests for evaluation storage exercise this method by
     * supplying valid and invalid score maps and asserting that: </p>
     * <ul>
     *   <li>invalid data results in appropriate error messages, and</li>
     *   <li>valid data produces the expected number of evaluation records in the
     *       database with correct student, parameter, score, weight, and evaluator. </li>
     * </ul>
     * 
     * @param student           the student being evaluated
     * @param scores            map from {@link EvaluationParameter} to the entered score
     * @param evaluatorUsername staff username performing the evaluation
     *
     * @throws IllegalArgumentException when any validation fails or a DB write fails
     */
    public static void saveEvaluation(User student,
                                      Map<EvaluationParameter, Integer> scores,
                                      String evaluatorUsername) {

        if (student == null) {
            throw new IllegalArgumentException("Please select a student to evaluate.");
        }
        if (scores == null || scores.isEmpty()) {
            throw new IllegalArgumentException("There are no parameters to evaluate.");
        }

        String studentUserName = student.getUserName();

        // Collect all validation errors so the user can fix multiple issues
        // in a single pass instead of encountering them one at a time.
        List<String> errorMessages = new ArrayList<>();

        for (Map.Entry<EvaluationParameter, Integer> entry : scores.entrySet()) {
            EvaluationParameter param = entry.getKey();
            Integer score = entry.getValue();

            if (param == null) continue;

            int weight = param.getWeight();
            String paramName = param.getName();

            if (score == null) {
                errorMessages.add("No score entered for parameter: " + paramName);
                continue;
            }

            if (score < 0 || score > weight) {
                errorMessages.add("Score for '" + paramName +
                        "' must be between 0 and " + weight + ".");
            }
        }

        if (!errorMessages.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errorMessages));
        }

        // If validation passes, insert evaluation records.
        // We insert one row per parameter instead of aggregating into a single
        // record so that parameters can be added/removed or re-weighted in the
        // future without changing the evaluationRecord schema.
        for (Map.Entry<EvaluationParameter, Integer> entry : scores.entrySet()) {
            EvaluationParameter param = entry.getKey();
            Integer score = entry.getValue();
            if (param == null || score == null) continue;

            boolean ok = theDatabase.createEvaluationRecord(
                studentUserName,
                param.getName(),
                score,
                param.getWeight(),
                evaluatorUsername
            );

            if (!ok) {
                // Fail fast on persistence errors so the caller can notify the user.
                // We do not attempt partial rollback here because evaluations are
                // inserted as independent records; higher-level logic can decide
                // whether to retry or clear partial data.
                throw new IllegalArgumentException(
                    "Unable to save evaluation for parameter: " + param.getName());
            }
        }
    }

    /**********
	 * <p> Method: performReturn() </p>
	 * 
	 * <p> Description: Return to the staff evaluation view for the current
	 * user. This method is designed to be called from the evaluation GUI
	 * when a "Return" action is triggered. </p>
	 * 
	 * <p> Data used and produced: </p>
	 * <ul>
	 *   <li>Uses {@code ViewStaffEvaluation.theStage} and
	 *       {@code ViewStaffEvaluation.theUser} maintained by the view.</li>
	 *   <li>Produces a refreshed evaluation view for the current staff user. </li>
	 * </ul>
	 * 
	 * <p> Validation and testing: GUI navigation tests ensure that invoking
	 * this method from the evaluation screen results in the expected view
	 * being displayed. </p>
	 */
	protected static void performReturn() {
		guiStaffEvaluation.ViewStaffEvaluation.displayStaffEvaluation(ViewStaffEvaluation.theStage,
				ViewStaffEvaluation.theUser);
	}
	
	
	/**********
	 * <p> Method: performLogout(Stage stage) </p>
	 * 
	 * <p> Description: Log out the current user from the staff evaluation flow
	 * and return to the normal login page, where existing users can log in or
	 * potential new users with an invitation code can start setting up an
	 * account. </p>
	 * 
	 * <p> Why this approach: The implementation reuses
     * {@code ViewStaffEvaluation.theStage} instead of the {@code stage}
     * parameter to stay consistent with the pattern used throughout the
     * FoundationsF25 GUI, where the active {@code Stage} is centrally tracked
     * by the view classes. This avoids subtle bugs that can arise when
     * multiple {@code Stage} instances are created or passed around. </p>
	 * 
	 * @param stage the current {@link Stage}; not used directly, retained for
	 *              signature consistency with other controllers
	 */
	protected static void performLogout(Stage stage) {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewStaffEvaluation.theStage);
	}
	
	
	/**********
	 * <p> Method: performQuit() </p>
	 * 
	 * <p> Description: Terminate the execution of the program from the staff
	 * evaluation context. When the application is restarted, it will begin at
	 * the normal login page. </p>
	 * 
	 * <p> Why this approach: Using {@code System.exit(0)} provides a simple and
	 * predictable shutdown mechanism for this single-user desktop application
	 * and matches the quit behavior implemented in other controllers. </p>
	 */
	protected static void performQuit() {
		System.exit(0);
	}
}
