package nl._42.heph.builder;

import java.io.Serializable;
import java.util.function.Supplier;

import nl._42.heph.AbstractBuildCommand;
import nl._42.heph.AbstractBuilder;
import nl._42.heph.BuilderConstructors;
import nl._42.heph.LazyEntityId;
import nl._42.heph.LazyEntityReference;
import nl._42.heph.domain.Organization;
import nl._42.heph.domain.Person;
import nl._42.heph.domain.PersonRepository;
import nl._42.heph.domain.Workspace;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public class PersonBuilder extends AbstractBuilder<Person, PersonBuilder.PersonBuildCommand> {

    public static final String EXPECTED_NAME = "Sjaak";

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private OrganizationBuilder organizationBuilder = new OrganizationBuilder();
    @Autowired
    private WorkspaceBuilder workspaceBuilder = new WorkspaceBuilder();

    @Override
    public PersonBuildCommand base() {
        return blank()
                .withName(EXPECTED_NAME)
                .withOrganization(organizationBuilder::_42)
                .withWorkspace(workspaceBuilder::my_workspace);
    }

    public Person sjaak() {
        return base()
                .create();
    }

    class PersonBuildCommand extends AbstractBuildCommand<Person> {

        @Override
        protected JpaRepository<Person, ? extends Serializable> getRepository() {
            return personRepository;
        }

        @Override
        protected Person findEntity(Person entity) {
            return personRepository.findByName(entity.getName());
        }

        public PersonBuildCommand withName(String name) {
            getInternalEntity().setName(name);
            return this;
        }

        public PersonBuildCommand withOrganization(Supplier<Organization> organizationReference) {
            // Sets the entity Organization on Person
            addBeforeCreateReference(new LazyEntityReference<>(
                    getInternalEntity()::getOrganization,
                    getInternalEntity()::setOrganization,
                    organizationReference
            ));
            return this;
        }

        public PersonBuildCommand withWorkspace(Supplier<Workspace> workspaceReference) {
            // Sets the ID of the Workspace on Person
            addBeforeCreateReference(new LazyEntityId<>(
                    getInternalEntity()::getWorkspaceId,
                    getInternalEntity()::setWorkspaceId,
                    workspaceReference
            ));
            return this;
        }

    }
}
