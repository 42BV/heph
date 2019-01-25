package nl._42.heph.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import nl._42.heph.domain.Organization;
import nl._42.heph.domain.OrganizationRepository;
import nl._42.heph.domain.Person;
import nl._42.heph.shared.AbstractSpringTest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PersonFixturesTest extends AbstractSpringTest {

    @Autowired
    private PersonFixturesRepository personRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private PersonFixtures personFixtures;

    @Test
    public void checkLazyCreation() {
        // Create a person with the same name by hand
        Person person = new Person();
        person.setName(PersonFixtures.EXPECTED_NAME);
        personRepository.save(person);

        personFixtures.base().create();

        List<Organization> organizations = organizationRepository.findAll();
        assertEquals(0, organizations.size());
    }

    @Test
    public void copy() {
        final String expectedName = "Somebody else";
        Person aPerson = personFixtures.sjaak();
        Person theOtherPerson = personFixtures.copy(aPerson)
                .withName(expectedName)
                .create();
        assertNotSame(aPerson.getId(), theOtherPerson.getId());
        assertNotSame(aPerson.getName(), theOtherPerson.getName());
    }

    @Test
    public void valueStore() {
        final AtomicInteger callbacksMade = new AtomicInteger(0);

        Person aPerson = personFixtures.base()
                .withName("aName")
                .withCallbackFunction((person) -> person.setName("aName #" + callbacksMade.incrementAndGet()))
                .create();

        assertEquals(1, callbacksMade.get());
        assertEquals("aName #1", aPerson.getName());
    }

    @Test
    public void createWithoutSpringContext() {
        assertCreationWithoutSpringContext(new PersonFixtures().base()::create);
    }

    @Test
    public void constructWithoutSpringContext() {
        assertCreationWithoutSpringContext(new PersonFixtures().base()::construct);
    }

    private void assertCreationWithoutSpringContext(Supplier<Person> personSupplier) {
        // Note that this test requires the OrganizationBuilder
        // in PersonBuilder to be manually constructed as well,
        // besides being @Autowired.
        Person person = personSupplier.get();
        assertEquals(PersonFixtures.EXPECTED_NAME, person.getName());
        assertEquals(OrganizationFixtures.EXPECTED_NAME, person.getOrganization().getName());
    }

}
