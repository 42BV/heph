package nl._42.heph.builder;

import java.util.Collection;
import java.util.function.Supplier;

import nl._42.heph.AbstractBuildCommand;
import nl._42.heph.domain.Organization;
import nl._42.heph.domain.OrganizationRepository;
import nl._42.heph.domain.Person;
import nl._42.heph.lazy.Resolve;
import nl._42.heph.lazy.ResolveStrategy;

public interface OrganizationBuildCommand extends AbstractBuildCommand<Organization, OrganizationRepository> {

    @Override
    default Organization findEntity(Organization entity) {
        return getRepository().findByName(entity.getName());
    }

    OrganizationBuildCommand withId(Long id);

    OrganizationBuildCommand withName(String name);

    // Various methods to test all logic of generated buildCommands...
    default OrganizationBuildCommand withCustomName(String customName) {
        getInternalEntity().setName("Custom name: " + customName);
        return this;
    }

    @Resolve(ResolveStrategy.BEFORE_FIND)
    OrganizationBuildCommand withOwner(Supplier<Person> ownerReference);

    // Array -> Collection
    OrganizationBuildCommand withContactPersons(Person... contactPersons);

    // Collection -> Collection
    OrganizationBuildCommand withContactPersons(Collection<?> contactPersons);

    // Array -> Array
    OrganizationBuildCommand withLegalIdentityNumbers(String[] legalIdentityNumbers);

    // Collection -> Array
    OrganizationBuildCommand withLegalIdentityNumbers(Collection<String> legalIdentityNumbers);

    // Primitive array
    OrganizationBuildCommand withLegalContract(byte[] legalContractFileContents);
}