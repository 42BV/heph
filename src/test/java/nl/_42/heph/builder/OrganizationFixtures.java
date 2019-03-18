package nl._42.heph.builder;

import java.util.Arrays;

import nl._42.heph.AbstractBuilder;
import nl._42.heph.domain.Organization;

import org.assertj.core.util.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrganizationFixtures extends AbstractBuilder<Organization, OrganizationBuildCommand> {

    public static final Long EXPECTED_ID        = 42L;
    public static final String EXPECTED_NAME    = "42BV";

    @Autowired
    private PersonFixtures personFixtures;

    @Override
    public OrganizationBuildCommand base() {
        return blank()
                .withId(EXPECTED_ID)
                .withName(EXPECTED_NAME);
    }

    public Organization _42() {
        return base()
                .create();
    }

    public Organization apple() {
        return base()
                .withName("Apple")
                .create();
    }

    public Organization banana() {
        return base()
                .withName("Banana")
                .withContactPersons(personFixtures.base().withName("A").create(), personFixtures.base().withName("B").create())
                .withLegalIdentityNumbers(new String[] {"42", "24"})
                .withLegalContract(new byte[] {42, 24})
                .create();
    }

    public Organization pie() {
        return blank()
                .withName("Pie")
                .withContactPersons(Arrays.asList(personFixtures.base().withName("C").create(), personFixtures.base().withName("D").create()))
                .withLegalIdentityNumbers(Arrays.asList("12", "34"))
                .create();
    }

    public Organization orange_customNamed() {
        return base()
                .withCustomName("Orange")
                .withOwner(() -> personFixtures.base().withName("E").create())
                .create();
    }

    public Organization orange_withMultipleContactPersons() {
        return blank()
                .withName("Orange")
                .withContactPersons(() -> Sets.newHashSet(Arrays.asList(personFixtures.base().withName("F").create(), personFixtures.base().withName("G").create())))
                .create();
    }
}
