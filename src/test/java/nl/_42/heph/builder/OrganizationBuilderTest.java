package nl._42.heph.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.util.List;

import nl._42.heph.domain.Organization;
import nl._42.heph.domain.OrganizationRepository;
import nl._42.heph.shared.AbstractSpringTest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class OrganizationBuilderTest extends AbstractSpringTest {

    @Autowired
    private OrganizationBuilder organizationBuilder;
    @Autowired
    private OrganizationRepository organizationRepository;

    @Test
    public void createSingleOrganization() {
        Organization organization = organizationBuilder.base().create();
        assertNotNull(organization.getId());
        assertEquals(OrganizationBuilder.EXPECTED_NAME, organization.getName());
    }

    @Test
    public void callOrganizationTwice_mustBeCreatedOnce() {
        organizationBuilder._42();
        Organization organization = organizationBuilder._42();

        List<Organization> organizations = organizationRepository.findAll();
        assertEquals(1, organizations.size());
        assertEquals(organization.getName(), organizations.get(0).getName());
    }

    @Test
    public void copy() {
        final String expectedName = "Fourty four";
        Organization fortyTwo = organizationBuilder._42();
        Organization fortyFour = organizationBuilder.copy(fortyTwo)
                .withName(expectedName)
                .create();
        assertNotSame(fortyTwo.getId(), fortyFour.getId());
        assertNotSame(fortyTwo.getName(), fortyFour.getName());
    }

}
