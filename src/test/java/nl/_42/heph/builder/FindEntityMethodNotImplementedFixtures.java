package nl._42.heph.builder;

import nl._42.heph.AbstractBuilder;
import nl._42.heph.domain.Person;

import org.springframework.stereotype.Component;

@Component
public class FindEntityMethodNotImplementedFixtures extends AbstractBuilder<Person, FindEntityMethodNotImplementedBuildCommand> {

    @Override
    public FindEntityMethodNotImplementedBuildCommand base() {
        return blank();
    }
}
