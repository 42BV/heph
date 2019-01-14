package nl._42.heph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import nl._42.heph.shared.AbstractSpringTest;

import org.junit.Test;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.repository.JpaRepository;

public class AbstractBuilderTest extends AbstractSpringTest {

    /**
     * Test if the generation of default builderConstructors fails if we only provide a custom constructor in our buildCommand and not override the
     * {@link AbstractBuilder#constructors()} method in the {@link AbstractBuilder} implementation.
     */
    @Test
    public void generateDefaultBuilderConstructors_shouldThrowError_becauseNoDefaultConstructorIsAvailable() {

        AbstractBuilder<MyEntity, MyBuildCommand> myBuilder = new AbstractBuilder<MyEntity, MyBuildCommand>() {
            @Override
            public MyBuildCommand base() {
                return new MyBuildCommand(42L);
            }
        };

        try {
            BuilderConstructors<MyEntity, MyBuildCommand> builderConstructors = myBuilder.constructors();
            builderConstructors.getConstructorTakingEntity().apply(new MyEntity(44L));
            fail("Expected BeanInstantiationException was not thrown");
        } catch (BeanInstantiationException e) {
            assertEquals("Failed to instantiate [nl._42.heph.AbstractBuilderTest$MyBuildCommand]: A no-args constructor is required!", e.getMessage());
        }

        try {
            BuilderConstructors<MyEntity, MyBuildCommand> builderConstructors = myBuilder.constructors();
            builderConstructors.getConstructorTakingSupplier().apply(() -> new MyEntity(42L));
            fail("Expected BeanInstantiationException was not thrown");
        } catch (BeanInstantiationException e) {
            assertEquals("Failed to instantiate [nl._42.heph.AbstractBuilderTest$MyBuildCommand]: A no-args constructor is required!", e.getMessage());
        }

        try {
            BuilderConstructors<MyEntity, MyBuildCommand> builderConstructors = myBuilder.constructors();
            builderConstructors.getEntityConstructor().get();
            fail("Expected BeanInstantiationException was not thrown");
        } catch (BeanInstantiationException e) {
            assertTrue(e.getMessage().contains("Failed to instantiate [nl._42.heph.AbstractBuilderTest$MyEntity]: No default constructor found;"));
        }

    }


    /**
     * Test if the usage of custom builderConstructors works as intended for a class of which no default BuilderConstructors can be generated.
     */
    @Test
    public void generateDefaultBuilderConstructors_shouldWorkCorrectly_becauseCustomConstructorsAreSpecified() {

        AbstractBuilder<MyEntity, MyBuildCommand> myBuilder = new AbstractBuilder<MyEntity, MyBuildCommand>() {
            @Override
            public MyBuildCommand base() {
                return new MyBuildCommand(42L);
            }

            @Override
            public BuilderConstructors<MyEntity, MyBuildCommand> constructors() {
                return new BuilderConstructors<>((e -> new MyBuildCommand(e.getId())), (e -> new MyBuildCommand(e.get().getId())), () -> new MyEntity(48L));
            }
        };

        BuilderConstructors<MyEntity, MyBuildCommand> builderConstructors = myBuilder.constructors();

        assertEquals(42L, builderConstructors.getConstructorTakingEntity().apply(new MyEntity(42L)).myLong, 0L);
        assertEquals(45L, builderConstructors.getConstructorTakingSupplier().apply(() -> new MyEntity(45L)).myLong, 0L);
        assertEquals(48L, builderConstructors.getEntityConstructor().get().getId(), 0);

    }

    private class MyEntity implements Persistable<Long> {

        private final Long id;

        private MyEntity(Long id) {
            this.id = id;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public boolean isNew() {
            return id != null;
        }
    }

    private class MyBuildCommand extends AbstractBuildCommand<MyEntity> {

        private final Long myLong;

        // This prevents a default construction from being available and should throw an error when generating the default builderConstructors
        private MyBuildCommand(Long myLong) {
            this.myLong = myLong;
        }

        @Override
        protected JpaRepository<MyEntity, Long> getRepository() {
            return null;
        }

        @Override
        protected MyEntity findEntity(MyEntity entity) {
            return entity;
        }
    }
}
