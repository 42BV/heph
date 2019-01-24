package nl._42.heph.builder;

import nl._42.heph.AbstractBuildCommand;
import nl._42.heph.domain.Person;
import nl._42.heph.domain.PersonRepository;

/**
 * Demo builder to test if the correct exception is thrown when 'findEntity' is not overridden in your BuildCommand.
 */
public interface FindEntityMethodNotImplementedBuildCommand extends AbstractBuildCommand<Person, PersonFixturesRepository> {
}
