package nl._42.heph.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;

public class Person implements Persistable<Long> {

    @Id
    private Long id;

    private Organization organization;
    private Long organizationId;

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
