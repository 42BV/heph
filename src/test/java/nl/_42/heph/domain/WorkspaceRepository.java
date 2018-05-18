package nl._42.heph.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    Workspace findByName(String name);
}
