package nl._42.heph.builder;

import nl._42.heph.AbstractBuildCommand;
import nl._42.heph.domain.Workspace;
import nl._42.heph.domain.WorkspaceRepository;

public interface WorkspaceBuildCommand extends AbstractBuildCommand<Workspace, WorkspaceRepository> {

    @Override
    default Workspace findEntity(Workspace entity) {
        return getRepository().findByName(entity.getName());
    }

    WorkspaceBuildCommand withName(String name);

}
