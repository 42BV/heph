package nl._42.heph.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;

public class Organization implements Persistable<Long> {

    @Id
    private Long id;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return id == null;
    }

}
