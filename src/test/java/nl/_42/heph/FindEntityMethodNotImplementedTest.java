package nl._42.heph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import nl._42.heph.builder.FindEntityMethodNotImplementedFixtures;
import nl._42.heph.shared.AbstractSpringTest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class FindEntityMethodNotImplementedTest extends AbstractSpringTest {

    @Autowired // Repository is required to test this exception.
    private FindEntityMethodNotImplementedFixtures findEntityMethodNotImplementedFixtures;

    @Test
    public void findEntityMethodNotImplementedInBuildCommand_shouldThrowCorrectException() {

        try {
            findEntityMethodNotImplementedFixtures.base().create();
            fail("Expected FindEntityMethodNotImplementedException was not thrown");
        } catch (FindEntityMethodNotImplementedException e) {
            assertEquals("Please override the 'findEntity()' method in your BuildCommand interface by using a default implementation.", e.getMessage());
        }
    }
}
