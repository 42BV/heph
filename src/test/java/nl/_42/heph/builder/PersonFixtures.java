package nl._42.heph.builder;

import nl._42.heph.AbstractBuilder;
import nl._42.heph.domain.Person;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class PersonFixtures extends AbstractBuilder<Person, PersonBuildCommand> {

    public static final String EXPECTED_NAME = "Sjaak";

    @Autowired
    @Lazy
    private OrganizationFixtures organizationFixtures = new OrganizationFixtures();
    @Autowired
    @Lazy
    private WorkspaceFixtures workspaceFixtures = new WorkspaceFixtures();

    @Override
    public PersonBuildCommand base() {
        return blank()
                .withName(EXPECTED_NAME)
                .withOrganization(organizationFixtures::_42)
                .withWorkspace(workspaceFixtures::my_workspace);
    }

    public Person sjaak() {
        return base()
                .create();
    }
}
