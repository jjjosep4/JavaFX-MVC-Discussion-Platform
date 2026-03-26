package prototypeTesting;

/**
 * A tiny prototype "service" class for creating evaluation parameters.
 *
 * This is where we enforce the rule that parameter names cannot be
 * empty or whitespace. The idea is that TP3 would later expand this
 * service to talk to the real Database and GUI layers.
 */
public class EvaluationParameterService {

    /**
     * Create a new EvaluationParameter.
     *
     * This method currently enforces just one important rule:
     * the parameter name must not be null, empty, or only whitespace.
     *
     * @param name        the name of the parameter (must be non-blank)
     * @param description a human-readable description of the parameter
     * @param weight      an integer weight used later during scoring
     * @return a new EvaluationParameter object if the input is valid
     * @throws IllegalArgumentException if the name is null, empty,
     *                                  or contains only whitespace
     */
    public EvaluationParameter createParameter(
            String name, String description, int weight) {

        // Simple validation rule for this prototype:
        // Reject null or empty names and/or descriptions.
        if (name == null || name.trim().isEmpty() || description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Parameter name must not be empty or blank");
        }

        // In TP3, we might add more validation here (e.g., weight range),
        // and eventually call into a real Database class. For this HW3
        // prototype, all we need to do is create the object and return it.
        return new EvaluationParameter(name, description, weight);
    }
}
