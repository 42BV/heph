package nl._42.heph;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Supplier;

import nl._42.heph.domain.Organization;
import nl._42.heph.domain.Person;
import nl._42.heph.lazy.LazyEntityId;

import org.junit.jupiter.api.Test;

public class LazyEntityIdTest {

    @Test
    public void setId() {
        final Long expectedId = 42L;
        Organization organization = new Organization();
        organization.setId(expectedId);
        Supplier<Organization> organizationSupplier = () -> organization;

        Person person = new Person();
        LazyEntityId lazyEntityId = new LazyEntityId<>(
                person::getOrganizationId,
                person::setOrganizationId,
                organizationSupplier);

        lazyEntityId.resolve();
        assertEquals(expectedId, person.getOrganizationId());
    }

}
