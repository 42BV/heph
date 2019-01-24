package nl._42.heph.builder;

import java.util.Collection;

import nl._42.heph.AbstractBuildCommand;

public interface MockSavedEntityBuildCommand extends AbstractBuildCommand<MockSavedEntity, MockSavedEntityRepository> {

    @Override
    default MockSavedEntity findEntity(MockSavedEntity entity) {
        Collection<MockSavedEntity> all = getRepository().findAll();

        if (all.contains(entity)) {
            return entity;
        }

        return null;
    }

    MockSavedEntityBuildCommand withAge(String version);
}
