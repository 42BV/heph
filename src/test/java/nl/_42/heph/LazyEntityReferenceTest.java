package nl._42.heph;

import static org.junit.jupiter.api.Assertions.assertEquals;

import nl._42.heph.builder.OrganizationFixtures;
import nl._42.heph.domain.Person;
import nl._42.heph.lazy.LazyEntityReference;

import org.junit.jupiter.api.Test;

public class LazyEntityReferenceTest {

    private OrganizationFixtures organizationFixtures = new OrganizationFixtures();

    @Test
    public void setReference() {
        Person person = new Person();
        LazyEntityReference lazyEntityReference = new LazyEntityReference<>(
                person::getOrganization,
                person::setOrganization,
                organizationFixtures::_42);

        lazyEntityReference.resolve();
        assertEquals(organizationFixtures._42().getName(), person.getOrganization().getName());
    }

    @Test
    public void valueAlreadySet_mayNotBeOverwritten() {
        Person person = new Person();
        person.setOrganization(organizationFixtures._42());

        LazyEntityReference lazyEntityReference = new LazyEntityReference<>(
                person::getOrganization,
                person::setOrganization,
                organizationFixtures::apple);

        lazyEntityReference.resolve();
        assertEquals(organizationFixtures._42().getName(), person.getOrganization().getName());
    }

    @Test
    public void illegalGetterPassed_mustWrite() {
        Person person = new Person();
        person.setOrganization(organizationFixtures._42());

        LazyEntityReference lazyEntityReference = new LazyEntityReference<>(
                null,
                person::setOrganization,
                organizationFixtures::apple);

        lazyEntityReference.resolve();
        assertEquals(organizationFixtures.apple().getName(), person.getOrganization().getName());
    }

    @Test
    public void nullReference_mustNotWrite() {
        Person person = new Person();
        person.setOrganization(organizationFixtures._42());

        LazyEntityReference lazyEntityReference = new LazyEntityReference<>(
                person::getOrganization,
                person::setOrganization,
                null);

        lazyEntityReference.resolve();
        assertEquals(organizationFixtures._42().getName(), person.getOrganization().getName());
    }

}
