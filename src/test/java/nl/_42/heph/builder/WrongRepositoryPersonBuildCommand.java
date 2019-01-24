package nl._42.heph.builder;

import nl._42.heph.AbstractBuildCommand;
import nl._42.heph.domain.Person;
import nl._42.heph.domain.PersonRepository;

/**
 * Test buildCommand referencing the wrong repository (PersonRepository instead of PersonFixturesRepository). Should throw {@link nl._42.heph.MultipleRepositoriesExistException} when used.
 */
public interface WrongRepositoryPersonBuildCommand extends AbstractBuildCommand<Person, PersonRepository> {

    @Override
    default Person findEntity(Person entity) {
        return getRepository().findByName(entity.getName());
    }

}
