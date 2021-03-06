package nl._42.heph;

import static org.junit.Assert.assertEquals;

import nl._42.heph.domain.Person;
import nl._42.heph.domain.PersonRepository;
import nl._42.heph.shared.AbstractSpringTest;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PersonRepositoryTest extends AbstractSpringTest {

    @Autowired
    private PersonRepository personRepository;

    @Test
    public void verifiesPersistenceInTestContext() {
        Person person = new Person();
        person.setName("Henk");
        personRepository.save(person);
        Assert.assertNotNull(person.getId());
        Person retrievedPerson = personRepository.findById(person.getId()).orElseThrow(() -> new IllegalStateException("Person was not saved in the database."));
        assertEquals(person.getName(), retrievedPerson.getName());
    }

}
