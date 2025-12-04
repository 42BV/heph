package nl._42.heph.builder;

import jakarta.persistence.Entity;
import nl._42.heph.shared.AbstractEntity;

/**
 * Entity for which no repository exists.
 */
@Entity
public class UnsavableEntity extends AbstractEntity {

    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
