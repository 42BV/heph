package nl._42.heph.builder;

import nl._42.heph.AbstractBuilder;
import nl._42.heph.domain.Workspace;

import org.springframework.stereotype.Component;

@Component
public class WorkspaceFixtures extends AbstractBuilder<Workspace, WorkspaceBuildCommand> {

    public static final String EXPECTED_NAME = "My workspace";

    @Override
    public WorkspaceBuildCommand base() {
        return blank()
                .withName(EXPECTED_NAME);
    }

    public Workspace my_workspace() {
        return base()
                .create();
    }
}
