package entityClasses;

/*******
 * <p> Title: EvaluationParameter Class. </p>
 *
 * <p> Description: Simple value object representing a single grading
 * parameter used by staff to evaluate student discussion performance.
 * Each parameter has a database-assigned primary key, a short name,
 * a free-text description, and an integer weight that contributes to
 * the overall 100-point evaluation scheme. </p>
 *
 * <p> Structure and interfaces: </p>
 * <ul>
 *   <li>Instances are created from database rows in
 *       {@code Database.getAllEvaluationParameters()} and passed to
 *       GUI controllers/views such as
 *       {@code ControllerStaffParameters} and
 *       {@code ViewStaffParameters}.</li>
 *   <li>The class exposes simple getters and setters for its fields so
 *       that controllers can update in-memory objects after successful
 *       database operations.</li>
 *   <li>The {@code id} field is the database primary key and is used
 *       for update and delete operations; the remaining fields are the
 *       user-visible attributes shown in the staff GUI. </li>
 * </ul>
 *
 * <p> Data used and produced: </p>
 * <ul>
 *   <li>Used by the staff parameter CRUD workflow to display and edit
 *       grading parameters.</li>
 *   <li>Passed into the staff evaluation logic to determine how much
 *       each parameter contributes to a student's total score.</li>
 * </ul>
 *
 * <p> Validation and testing: The correctness of this class is validated
 * indirectly by the staff parameter tests (for example,
 * {@code ControllerStaffParametersTest} and
 * {@code ViewStaffParametersTest}), which verify that the controller
 * and views correctly read, display, and update {@code EvaluationParameter}
 * instances against the database. </p>
 *
 * @author
 * @version 1.00
 */
public class EvaluationParameter {

    /**
     * Database-assigned primary key used for updates/deletes.
     * We explicitly keep a separate ID rather than relying on the
     * name field so that parameters can be renamed without breaking
     * existing records that reference this row.
     */
    private int id;

    private String name;
    private String description;
    private int weight;

    /*******
     * <p> Constructor: EvaluationParameter </p>
     *
     * <p> Description: Build a fully-initialized parameter instance,
     * typically from a database result set. The {@code id} is the
     * row's primary key; {@code name}, {@code description}, and
     * {@code weight} are user-visible attributes. </p>
     *
     * @param id          primary key from the database table
     * @param name        short name shown in the staff GUI
     * @param description free-text explanation of what is being graded
     * @param weight      integer weight used in the 100-point scheme
     */
    public EvaluationParameter(int id, String name, String description, int weight) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.weight = weight;
    }

    // Getters – simple property accessors used by table bindings and controllers

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getWeight() { return weight; }

    // Setters – used after successful DB updates so in-memory objects stay in sync

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setWeight(int weight) { this.weight = weight; }
}
