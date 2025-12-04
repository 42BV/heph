package nl._42.heph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import nl._42.heph.domain.Person;
import nl._42.heph.domain.PersonRepository;
import nl._42.heph.shared.AbstractSpringTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PersonRepositoryTest extends AbstractSpringTest {

    @Autowired
    private PersonRepository personRepository;

    @Test
    public void verifiesPersistenceInTestContext() {
        Person person = new Person();
        person.setName("Henk");
        personRepository.save(person);
        assertNotNull(person.getId());
        Person retrievedPerson = personRepository.findById(person.getId()).orElseThrow(() -> new IllegalStateException("Person was not saved in the database."));
        assertEquals(person.getName(), retrievedPerson.getName());
    }

}
