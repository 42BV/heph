package nl._42.heph;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import nl._42.heph.builder.PersonFixtures;
import nl._42.heph.domain.Person;
import nl._42.heph.shared.AbstractSpringTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class NoBeanMapperTest extends AbstractSpringTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PersonFixtures personFixtures;

    /**
     * This test ensures that the library throws the correct error message if BeanMapper is not on our classpath.
     */
    @Test
    public void noBeanMapperOnClassPath_copyFunctionShouldThrowErrorExplainingHowToResolve() {
        ClassLoader originalClassLoader = applicationContext.getClassLoader();

        try {
            ((GenericApplicationContext)applicationContext).setClassLoader(new NoBeanMapperMockClassLoader());

            Person person = personFixtures.sjaak();

            try {
                personFixtures.copy(person);
                fail("Expected UnsupportedOperationException was not thrown!");
            } catch (UnsupportedOperationException e) {
                assertEquals("The copy feature requires the io.beanmapper dependency to be added to your project. Please include it and then try again.", e.getMessage());
            }
        } finally {
            ((GenericApplicationContext) applicationContext).setClassLoader(originalClassLoader);
        }

    }
}
