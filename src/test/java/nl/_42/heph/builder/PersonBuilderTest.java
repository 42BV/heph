package nl._42.heph.builder;

import static nl._42.heph.builder.PersonBuilder.EXPECTED_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.List;

import nl._42.heph.domain.Organization;
import nl._42.heph.domain.OrganizationRepository;
import nl._42.heph.domain.Person;
import nl._42.heph.domain.PersonRepository;
import nl._42.heph.shared.AbstractSpringTest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PersonBuilderTest extends AbstractSpringTest {

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private PersonBuilder personBuilder;

    @Test
    public void checkLazyCreation() {
        // Create a person with the same name by hand
        Person person = new Person();
        person.setName(PersonBuilder.EXPECTED_NAME);
        personRepository.save(person);

        personBuilder.base().create();

        List<Organization> organizations = organizationRepository.findAll();
        assertEquals(0, organizations.size());
    }

    @Test
    public void copy() {
        final String expectedName = "Somebody else";
        Person aPerson = personBuilder.sjaak();
        Person theOtherPerson = personBuilder.copy(aPerson)
                .withName(expectedName)
                .create();
        assertNotSame(aPerson.getId(), theOtherPerson.getId());
        assertNotSame(aPerson.getName(), theOtherPerson.getName());
    }

    @Test
    public void constructWithoutSpringContext() {
        // Note that this test requires the OrganizationBuilder
        // in PersonBuilder to be manually constructed as well,
        // besides being @Autowired.
        PersonBuilder personBuilder = new PersonBuilder();
        Person person = personBuilder.sjaak();
        assertEquals(PersonBuilder.EXPECTED_NAME, person.getName());
        assertEquals(OrganizationBuilder.EXPECTED_NAME, person.getOrganization().getName());
    }

}
