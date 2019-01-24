package nl._42.heph.builder;

import nl._42.heph.AbstractBuilder;

public class MockSavedEntityFixtures extends AbstractBuilder<MockSavedEntity, MockSavedEntityBuildCommand> {

    @Override
    public MockSavedEntityBuildCommand base() {
        return blank().withAge("42");
    }
}
