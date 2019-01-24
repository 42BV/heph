package nl._42.heph.builder;

import nl._42.heph.shared.AbstractEntity;

/**
 * Test entity which does not get saved to the database, but to a list in {@link nl._42.heph.shared.MockRepository}
 */
public class MockSavedEntity extends AbstractEntity {

    private String age;

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}
