package nl._42.heph.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import nl._42.heph.shared.AbstractEntity;

@Entity
public class Organization extends AbstractEntity {

    @ManyToOne
    private Person owner;

    @OneToMany(mappedBy = "organization")
    private Set<Person> contactPersons;

    @ElementCollection(targetClass = String.class)
    @OrderColumn(name = "legal_identity_number_sequence")
    @CollectionTable(name = "organization_legal_identity_number", joinColumns = @JoinColumn(name = "organization_id"))
    @Column(name = "legal_identity_number")
    private List<String> legalIdentityNumbers = new ArrayList<>();

    private byte[] legalContract;

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    private String name;

    public Set<Person> getContactPersons() {
        return contactPersons;
    }

    public void setContactPersons(Set<Person> contactPersons) {
        this.contactPersons = contactPersons;
    }

    public List<String> getLegalIdentityNumbers() {
        return legalIdentityNumbers;
    }

    public void setLegalIdentityNumbers(List<String> legalIdentityNumbers) {
        this.legalIdentityNumbers = legalIdentityNumbers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getLegalContract() {
        return legalContract;
    }

    public void setLegalContract(byte[] legalContract) {
        this.legalContract = legalContract;
    }
}
