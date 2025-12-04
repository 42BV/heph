package nl._42.heph.domain;

import jakarta.persistence.Entity;
import nl._42.heph.shared.AbstractEntity;

@Entity
public class Workspace extends AbstractEntity {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
