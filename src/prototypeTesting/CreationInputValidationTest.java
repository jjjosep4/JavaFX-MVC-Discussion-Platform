package prototypeTesting;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Simple HW3 prototype tests for the Staff Parameter CRUD aspect.
 *
 * This test focuses on one small part of the requirement:
 * we should not be able to create an evaluation parameter
 * if the name is empty or only whitespace.
 *
 * This is written in a TDD style: we write this test first,
 * then write just enough code in EvaluationParameterService
 * to make it pass.
 */
public class CreationInputValidationTest {

    /**
     * Test: createParameter should reject an empty name.
     *
     * This covers the "invalid creation" case for parameters with
     * an empty or blank name. We expect the method to throw an
     * IllegalArgumentException instead of silently creating
     * a bad parameter.
     */
    @Test
    public void testCreateParameter_EmptyNameRejected() {
        EvaluationParameterService service = new EvaluationParameterService();

        // Given: an empty name and otherwise valid description/weight
        String name = "";
        String description = "Measures clarity";
        int weight = 20;

        // When / Then: the service should reject this with an exception.
        assertThrows(IllegalArgumentException.class, () -> {
            service.createParameter(name, description, weight);
        }, "Empty parameter names should cause an IllegalArgumentException");
    }

    /**
     * Test: createParameter should reject an empty description.
     *
     * This covers the "invalid creation" case for parameters with
     * an empty or blank description. We expect the method to throw an
     * IllegalArgumentException instead of silently creating
     * a bad parameter.
     */
    @Test
    public void testCreateParameter_EmptyDescriptionRejected() {
        EvaluationParameterService service = new EvaluationParameterService();

        // Given: an empty description and otherwise valid name/weight
        String name = "Clarity";
        String description = "";
        int weight = 20;

        // When / Then: the service should reject this with an exception.
        assertThrows(IllegalArgumentException.class, () -> {
            service.createParameter(name, description, weight);
        }, "Empty parameter description should cause an IllegalArgumentException");
    }
    

    /**
     * Test: createParameter should accept a simple, valid name.
     *
     * This is the positive case for the same requirement: we
     * want to see that a normal, non-empty name works and
     * returns a proper EvaluationParameter object.
     */
    @Test
    public void testCreateParameter_ValidNameAccepted() {
        EvaluationParameterService service = new EvaluationParameterService();

        String name = "Clarity";
        String description = "How clear the student's post is";
        int weight = 20;

        EvaluationParameter param =
                service.createParameter(name, description, weight);

        // Basic checks on the object that comes back:
        assertNotNull(param, "A valid parameter should be created");
        assertEquals(name, param.getName(), "Name should match the input");
        assertEquals(description, param.getDescription(),
                "Description should match the input");
        assertEquals(weight, param.getWeight(),
                "Weight should match the input");
    }
}
