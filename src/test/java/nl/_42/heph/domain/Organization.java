package nl._42.heph.domain;

import javax.persistence.Entity;

import nl._42.heph.shared.AbstractEntity;

@Entity
public class Organization extends AbstractEntity {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
