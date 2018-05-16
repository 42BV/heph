package nl._42.heph;

import static org.junit.Assert.assertEquals;

import nl._42.heph.builder.OrganizationBuilder;
import nl._42.heph.domain.Person;

import org.junit.Test;

public class LazyEntityReferenceTest {

    private OrganizationBuilder organizationBuilder = new OrganizationBuilder();

    @Test
    public void setReference() {
        Person person = new Person();
        LazyEntityReference lazyEntityReference = new LazyEntityReference<>(
                person::getOrganization,
                person::setOrganization,
                organizationBuilder::_42);

        lazyEntityReference.resolve();
        assertEquals(organizationBuilder._42().getName(), person.getOrganization().getName());
    }

    @Test
    public void valueAlreadySet_mayNotBeOverwritten() {
        Person person = new Person();
        person.setOrganization(organizationBuilder._42());

        LazyEntityReference lazyEntityReference = new LazyEntityReference<>(
                person::getOrganization,
                person::setOrganization,
                organizationBuilder::apple);

        lazyEntityReference.resolve();
        assertEquals(organizationBuilder._42().getName(), person.getOrganization().getName());
    }

    @Test
    public void illegalGetterPassed_mustWrite() {
        Person person = new Person();
        person.setOrganization(organizationBuilder._42());

        LazyEntityReference lazyEntityReference = new LazyEntityReference<>(
                null,
                person::setOrganization,
                organizationBuilder::apple);

        lazyEntityReference.resolve();
        assertEquals(organizationBuilder.apple().getName(), person.getOrganization().getName());
    }

    @Test
    public void nullReference_mustNotWrite() {
        Person person = new Person();
        person.setOrganization(organizationBuilder._42());

        LazyEntityReference lazyEntityReference = new LazyEntityReference<>(
                person::getOrganization,
                person::setOrganization,
                null);

        lazyEntityReference.resolve();
        assertEquals(organizationBuilder._42().getName(), person.getOrganization().getName());
    }

}
