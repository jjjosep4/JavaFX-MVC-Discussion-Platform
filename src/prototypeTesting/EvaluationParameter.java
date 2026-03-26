package prototypeTesting;

/**
 * A tiny prototype class that represents one evaluation parameter.
 *
 * This is not the full TP3 version; it only contains what is needed
 * for HW3 Task 5.1 to show how we can prevent creation of parameters
 * with empty names using TDD.
 */
public class EvaluationParameter {

    /** The display name for this parameter (e.g., "Clarity"). */
    private String name;

    /** A short description explaining what this parameter measures. */
    private String description;

    /** A simple integer weight (e.g., 0 - 100) for this parameter. */
    private int weight;

    /**
     * Construct a new EvaluationParameter.
     *
     * @param name        the name of the parameter (must be non-blank)
     * @param description a short explanation of what we are measuring
     * @param weight      an integer weight that will be used when grading
     */
    public EvaluationParameter(String name, String description, int weight) {
        this.name = name;
        this.description = description;
        this.weight = weight;
    }

    /** @return the parameter's name */
    public String getName() {
        return name;
    }

    /** @return the parameter's description */
    public String getDescription() {
        return description;
    }

    /** @return the parameter's weight */
    public int getWeight() {
        return weight;
    }
}
