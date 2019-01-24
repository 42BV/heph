package nl._42.heph.builder;

import nl._42.heph.domain.Person;
import nl._42.heph.domain.PersonRepository;

/**
 * Special version of {@link nl._42.heph.domain.PersonRepository} which contains a method to uniquely identify a person to build fixtures.
 * This class exists to avoid having to ship this otherwise-unused repository method in the production codebase.
 */
public interface PersonFixturesRepository extends PersonRepository {

    Person findByNameAndOrganizationIdAndWorkspaceId(String name, Long organisationId, Long workspaceId);

}
