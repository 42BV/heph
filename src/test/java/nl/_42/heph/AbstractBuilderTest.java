package nl._42.heph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.lang.reflect.Field;

import nl._42.heph.domain.Person;
import nl._42.heph.shared.AbstractSpringTest;

import org.junit.Test;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.util.ReflectionUtils;

public class AbstractBuilderTest extends AbstractSpringTest {

    @Autowired
    private ApplicationContext applicationContext;

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
            assertEquals("Failed to instantiate [nl._42.heph.AbstractBuilderTest$MyBuildCommand]: The class may not be private and a no-args constructor (present by default) is required!", e.getMessage());
        }

        try {
            BuilderConstructors<MyEntity, MyBuildCommand> builderConstructors = myBuilder.constructors();
            builderConstructors.getConstructorTakingSupplier().apply(() -> new MyEntity(42L));
            fail("Expected BeanInstantiationException was not thrown");
        } catch (BeanInstantiationException e) {
            assertEquals("Failed to instantiate [nl._42.heph.AbstractBuilderTest$MyBuildCommand]: The class may not be private and a no-args constructor (present by default) is required!", e.getMessage());
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

    @Test
    public void generateRepositorySupplierFunction_repositoryAvailable_shouldReturnRepositoryAndUnsetLookupFunction() {

        TestPersonBuilder myBuilder = new TestPersonBuilder();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(myBuilder);

        // Test if a repository lookup function has been generated
        TestPersonBuilder.TestPersonBuildCommand testPersonBuildCommand = myBuilder.constructors().getConstructorTakingEntity().apply(new Person());

        Field repositorySupplierField = ReflectionUtils.findField(MyBuildCommand.class, "repositorySupplier");
        assertNotNull(repositorySupplierField);

        repositorySupplierField.setAccessible(true);

        assertNotNull(ReflectionUtils.getField(repositorySupplierField, testPersonBuildCommand));

        // There is a repository for 'MyEntity'. The call to getRepository() should return the repository and remove the repository lookup function from this instance of MyBuildCommand
        JpaRepository<Person, ? extends Serializable> repository = testPersonBuildCommand.getRepository();
        assertNotNull(repository);
        assertEquals(0, repository.findAll().size());

        myBuilder.base().create();
        assertEquals(1, repository.findAll().size());

        assertNull(ReflectionUtils.getField(repositorySupplierField, testPersonBuildCommand));

        // Even without lookup function it should still return the repository.
        assertEquals(repository, testPersonBuildCommand.getRepository());
    }

    @Test
    public void generateRepositorySupplierFunction_noRepositoryAvailable_shouldReturnNullAndUnsetLookupFunction() {
        AbstractBuilder<MyEntity, MyBuildCommand> myBuilder = new AbstractBuilder<MyEntity, MyBuildCommand>() {

            @Override
            public MyBuildCommand base() {
                return new MyBuildCommand(42L);
            }

            // Required to override, as myBuildCommand does not have the required no-args constructor to auto-generate BuilderConstructors with.
            @Override
            public BuilderConstructors<MyEntity, MyBuildCommand> constructors() {
                return new BuilderConstructors<>((e -> new MyBuildCommand(e.getId())), (e -> new MyBuildCommand(e.get().getId())), () -> new MyEntity(48L));
            }
        };

        // Test if a repository lookup function has been generated
        MyBuildCommand myBuildCommand = myBuilder.constructors().getConstructorTakingEntity().apply(new MyEntity(42L));

        Field repositorySupplierField = ReflectionUtils.findField(MyBuildCommand.class, "repositorySupplier");
        assertNotNull(repositorySupplierField);

        repositorySupplierField.setAccessible(true);

        assertNotNull(ReflectionUtils.getField(repositorySupplierField, myBuildCommand));

        // There is no repository for 'MyEntity'. The call to getRepository() should return null and remove the repository lookup function from this instance of MyBuildCommand
        assertNull(myBuildCommand.getRepository());
        assertNull(ReflectionUtils.getField(repositorySupplierField, myBuildCommand));

        // Even without lookup function it should still return null.
        assertNull(myBuildCommand.getRepository());
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
        protected MyEntity findEntity(MyEntity entity) {
            return entity;
        }
    }

    // Test / mock version of PersonBuilder which always saves entities to the database.
    private class TestPersonBuilder extends AbstractBuilder<Person, TestPersonBuilder.TestPersonBuildCommand> {

        @Override
        public TestPersonBuildCommand base() {
            return blank();
        }

        class TestPersonBuildCommand extends AbstractBuildCommand<Person> {

            @Override
            protected Person findEntity(Person entity) {
                return null; // Force repository to save new entity.
            }
        }
    }


}
