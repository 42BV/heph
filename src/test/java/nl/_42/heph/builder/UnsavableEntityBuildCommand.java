package nl._42.heph.builder;

import nl._42.heph.AbstractBuildCommand;
import nl._42.heph.NoOpBeanSaver;

public interface UnsavableEntityBuildCommand extends AbstractBuildCommand<UnsavableEntity, NoOpBeanSaver> {

    @Override
    default UnsavableEntity findEntity(UnsavableEntity entity) {
        return null;
    }

    UnsavableEntityBuildCommand withVersion(String version);
}
