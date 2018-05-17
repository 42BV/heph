package nl._42.heph.domain;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import nl._42.heph.shared.AbstractEntity;

@Entity
public class Person extends AbstractEntity {

    @ManyToOne
    private Organization organization;
    private Long organizationId;
    private String name;

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
