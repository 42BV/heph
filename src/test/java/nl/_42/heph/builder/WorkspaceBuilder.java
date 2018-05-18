package nl._42.heph.builder;

import java.io.Serializable;
import java.util.function.Supplier;

import nl._42.heph.AbstractBuildCommand;
import nl._42.heph.AbstractBuilder;
import nl._42.heph.BuilderConstructors;
import nl._42.heph.domain.Workspace;
import nl._42.heph.domain.WorkspaceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceBuilder extends AbstractBuilder<Workspace, WorkspaceBuilder.WorkspaceBuildCommand> {

    public static final String EXPECTED_NAME = "My workspace";

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Override
    public BuilderConstructors<Workspace, WorkspaceBuildCommand> constructors() {
        return new BuilderConstructors<>(
                WorkspaceBuildCommand::new,
                WorkspaceBuildCommand::new,
                Workspace::new
        );
    }

    @Override
    public WorkspaceBuildCommand base() {
        return blank()
                .withName(EXPECTED_NAME);
    }

    public Workspace my_workspace() {
        return base()
                .create();
    }

    class WorkspaceBuildCommand extends AbstractBuildCommand<Workspace> {

        public WorkspaceBuildCommand(Workspace entity) {
            super(entity);
        }

        public WorkspaceBuildCommand(Supplier<Workspace> entity) {
            super(entity);
        }

        @Override
        protected JpaRepository<Workspace, ? extends Serializable> getRepository() {
            return workspaceRepository;
        }

        @Override
        protected Workspace findEntity(Workspace entity) {
            return workspaceRepository.findByName(entity.getName());
        }

        public WorkspaceBuildCommand withName(String name) {
            getInternalEntity().setName(name);
            return this;
        }
    }
}
