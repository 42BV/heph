package nl._42.heph.builder;

import nl._42.heph.AbstractBuilder;
import nl._42.heph.domain.Person;

import org.springframework.stereotype.Component;

@Component
public class WrongRepositoryPersonFixtures extends AbstractBuilder<Person, WrongRepositoryPersonBuildCommand> {

    @Override
    public WrongRepositoryPersonBuildCommand base() {
        return blank();
    }
}
