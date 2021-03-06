package nl._42.heph.builder;

import nl._42.heph.AbstractBuilder;

public class UnsavableEntityFixtures extends AbstractBuilder<UnsavableEntity, UnsavableEntityBuildCommand> {

    private static final String EXPECTED_VERSION = "1.0.0";

    @Override
    public UnsavableEntityBuildCommand base() {
        return blank()
                .withVersion(EXPECTED_VERSION);
    }
}
