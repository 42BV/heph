package nl._42.heph.builder;

import java.io.Serializable;
import java.util.function.Supplier;

import nl._42.heph.AbstractBuildCommand;
import nl._42.heph.AbstractBuilder;
import nl._42.heph.BuilderConstructors;
import nl._42.heph.domain.Organization;
import nl._42.heph.domain.OrganizationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public class OrganizationBuilder extends AbstractBuilder<Organization, OrganizationBuilder.OrganizationBuildCommand> {

    public static final Long EXPECTED_ID        = 42L;
    public static final String EXPECTED_NAME    = "42BV";

    @Autowired
    private OrganizationRepository organizationRepository;

    @Override
    public BuilderConstructors<Organization, OrganizationBuildCommand> constructors() {
        return new BuilderConstructors<>(
                OrganizationBuildCommand::new,
                OrganizationBuildCommand::new,
                Organization::new
        );
    }

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

    class OrganizationBuildCommand extends AbstractBuildCommand<Organization> {

        public OrganizationBuildCommand(Organization entity) {
            super(entity);
        }

        public OrganizationBuildCommand(Supplier<Organization> entity) {
            super(entity);
        }

        @Override
        protected JpaRepository<Organization, ? extends Serializable> getRepository() {
            return organizationRepository;
        }

        @Override
        protected Organization findEntity(Organization entity) {
            return organizationRepository.findByName(entity.getName());
        }

        public OrganizationBuildCommand withId(Long id) {
            getInternalEntity().setId(id);
            return this;
        }

        public OrganizationBuildCommand withName(String name) {
            getInternalEntity().setName(name);
            return this;
        }

    }
}
