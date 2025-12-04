package nl._42.heph.builder;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import nl._42.heph.domain.Organization;
import nl._42.heph.domain.OrganizationRepository;
import nl._42.heph.shared.AbstractSpringTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class OrganizationFixturesTest extends AbstractSpringTest {

    @Autowired
    private OrganizationFixtures organizationFixtures;
    @Autowired
    private OrganizationRepository organizationRepository;

    @Test
    public void createSingleOrganization() {
        Organization organization = organizationFixtures.base().create();
        assertNotNull(organization.getId());
        assertEquals(OrganizationFixtures.EXPECTED_NAME, organization.getName());
    }

    @Test
    public void callOrganizationTwice_mustBeCreatedOnce() {
        organizationFixtures._42();
        Organization organization = organizationFixtures._42();

        List<Organization> organizations = organizationRepository.findAll();
        assertEquals(1, organizations.size());
        assertEquals(organization.getName(), organizations.getFirst().getName());
    }

    @Test
    public void collectionMappings_shouldAddProperlyToCollectionsAndAppendIfNeeded() {
        Organization banana = organizationFixtures.banana();

        assertEquals(2, banana.getContactPersons().size());
        assertTrue(banana.getContactPersons().stream().anyMatch(p -> p.getName().equals("A")));
        assertTrue(banana.getContactPersons().stream().anyMatch(p -> p.getName().equals("B")));
        assertArrayEquals(new String[] {"42", "24"}, banana.getLegalIdentityNumbers().toArray());

        Organization pie = organizationFixtures.pie();
        assertEquals(2, pie.getContactPersons().size());
        assertTrue(pie.getContactPersons().stream().anyMatch(p -> p.getName().equals("C")));
        assertTrue(pie.getContactPersons().stream().anyMatch(p -> p.getName().equals("D")));
        assertArrayEquals(new String[] {"12", "34"}, pie.getLegalIdentityNumbers().toArray());

        Organization custom = organizationFixtures.base()
                .withName("Lemon")
                .withLegalIdentityNumbers(new String[] {"42", "24"})
                .withLegalIdentityNumbers(new String[] {"48"})
                .create();

        assertArrayEquals(new String[] {"42", "24", "48"}, custom.getLegalIdentityNumbers().toArray());
    }

    @Test
    public void collectionMappings_shouldResolveSuppliedCollectionCorrectly() {
        Organization orangeWithMultipleContactPersons = organizationFixtures.orange_withMultipleContactPersons();

        assertEquals("Orange", orangeWithMultipleContactPersons.getName());
        assertEquals(2, orangeWithMultipleContactPersons.getContactPersons().size());
        assertTrue(orangeWithMultipleContactPersons.getContactPersons().stream().anyMatch(p -> p.getName().equals("F")));
        assertTrue(orangeWithMultipleContactPersons.getContactPersons().stream().anyMatch(p -> p.getName().equals("G")));
    }

    @Test
    public void customWithMethod_suppliedOwner_shouldExecuteWithMethodAndResolveBeforeFindReference() {
        Organization customOrange = organizationFixtures.orange_customNamed();

        assertEquals("Custom name: Orange", customOrange.getName());
        assertEquals("E", customOrange.getOwner().getName());
    }

    @Test
    public void copy() {
        final String expectedName = "Fourty four";
        Organization fortyTwo = organizationFixtures._42();
        Organization fortyFour = organizationFixtures.copy(fortyTwo)
                .withName(expectedName)
                .create();
        assertNotSame(fortyTwo.getId(), fortyFour.getId());
        assertNotSame(fortyTwo.getName(), fortyFour.getName());
    }

}
