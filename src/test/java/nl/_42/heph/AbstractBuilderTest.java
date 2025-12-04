package nl._42.heph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import nl._42.heph.builder.MockSavedEntity;
import nl._42.heph.builder.MockSavedEntityBuildCommand;
import nl._42.heph.builder.MockSavedEntityFixtures;
import nl._42.heph.builder.MockSavedEntityRepository;
import nl._42.heph.builder.PersonBuildCommand;
import nl._42.heph.builder.PersonFixtures;
import nl._42.heph.builder.PersonFixturesRepository;
import nl._42.heph.builder.UnsavableEntity;
import nl._42.heph.builder.UnsavableEntityBuildCommand;
import nl._42.heph.builder.UnsavableEntityFixtures;
import nl._42.heph.builder.WrongRepositoryPersonFixtures;
import nl._42.heph.domain.Person;
import nl._42.heph.shared.AbstractSpringTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

public class AbstractBuilderTest extends AbstractSpringTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @SuppressWarnings({ "unchecked" })
    public void generateRepositorySupplier_jpaRepositoryAvailable_shouldReturnRepositoryAndUnsetLookupFunction() throws Throwable {
        PersonFixtures personFixtures = new PersonFixtures();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(personFixtures);

        // Test if a repository lookup function has been generated
        Method constructorsMethod = ReflectionUtils.findMethod(AbstractBuilder.class, "constructors");
        assertNotNull(constructorsMethod);
        ReflectionUtils.makeAccessible(constructorsMethod);

        BuilderConstructors<Person, PersonBuildCommand> constructors = (BuilderConstructors<Person, PersonBuildCommand>) ReflectionUtils.invokeMethod(constructorsMethod, personFixtures);
        assertNotNull(constructors);
        PersonBuildCommand personBuildCommand = constructors.getConstructorTakingEntity().apply(new Person());

        Method getRepositorySupplierMethod = ReflectionUtils.findMethod(DefaultBuildCommand.class, "getRepositorySupplier");
        assertNotNull(getRepositorySupplierMethod);
        ReflectionUtils.makeAccessible(getRepositorySupplierMethod);

        InvocationHandler handler = Proxy.getInvocationHandler(personBuildCommand);
        assertNotNull(handler.invoke(personBuildCommand, getRepositorySupplierMethod, new Object[0]));

        // There is a repository for 'Person'. The call to getRepository() should return the repository and remove the repository lookup function from this instance of PersonBuildCommand
        PersonFixturesRepository repository = personBuildCommand.getRepository();
        assertNotNull(repository);
        assertEquals(0, repository.findAll().size());

        personFixtures.base().create();
        assertEquals(1, repository.findAll().size());


        // Even without lookup function it should still return the repository.
        assertNull(handler.invoke(personBuildCommand, getRepositorySupplierMethod, new Object[0]));
        assertEquals(repository, personBuildCommand.getRepository());
    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void generateRepositorySupplier_beanSaverAvailable_shouldReturnRepositoryAndUnsetLookupFunction() throws Throwable {
        MockSavedEntityFixtures mockSavedEntityFixtures = new MockSavedEntityFixtures();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(mockSavedEntityFixtures);

        // Test if a repository lookup function has been generated
        Method constructorsMethod = ReflectionUtils.findMethod(AbstractBuilder.class, "constructors");
        assertNotNull(constructorsMethod);
        ReflectionUtils.makeAccessible(constructorsMethod);

        BuilderConstructors<MockSavedEntity, MockSavedEntityBuildCommand> constructors = (BuilderConstructors<MockSavedEntity, MockSavedEntityBuildCommand>) ReflectionUtils.invokeMethod(constructorsMethod, mockSavedEntityFixtures);
        assertNotNull(constructors);
        MockSavedEntityBuildCommand mockSavedEntityBuildCommand = constructors.getConstructorTakingEntity().apply(new MockSavedEntity());

        Method getRepositorySupplierMethod = ReflectionUtils.findMethod(DefaultBuildCommand.class, "getRepositorySupplier");
        assertNotNull(getRepositorySupplierMethod);
        ReflectionUtils.makeAccessible(getRepositorySupplierMethod);

        InvocationHandler handler = Proxy.getInvocationHandler(mockSavedEntityBuildCommand);
        assertNotNull(handler.invoke(mockSavedEntityBuildCommand, getRepositorySupplierMethod, new Object[0]));

        // There is a repository for 'Person'. The call to getRepository() should return the repository and remove the repository lookup function from this instance of PersonBuildCommand
        MockSavedEntityRepository repository = mockSavedEntityBuildCommand.getRepository();
        assertNotNull(repository);
        assertEquals(0, repository.findAll().size());

        mockSavedEntityFixtures.base().create();
        assertEquals(1, repository.findAll().size());


        // Even without lookup function it should still return the repository.
        assertNull(handler.invoke(mockSavedEntityBuildCommand, getRepositorySupplierMethod, new Object[0]));
        assertEquals(repository, mockSavedEntityBuildCommand.getRepository());
    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void generateRepositorySupplier_noRepositoryAvailable_shouldReturnNullAndUnsetLookupFunction() throws Throwable {
        // Test if a repository lookup function has been generated
        UnsavableEntityFixtures unsavableEntityFixtures = new UnsavableEntityFixtures();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(unsavableEntityFixtures);

        Method constructorsMethod = ReflectionUtils.findMethod(AbstractBuilder.class, "constructors");
        assertNotNull(constructorsMethod);
        ReflectionUtils.makeAccessible(constructorsMethod);

        BuilderConstructors<UnsavableEntity, UnsavableEntityBuildCommand> constructors = (BuilderConstructors<UnsavableEntity, UnsavableEntityBuildCommand>) ReflectionUtils.invokeMethod(constructorsMethod, unsavableEntityFixtures);
        assertNotNull(constructors);

        UnsavableEntityBuildCommand unsavableEntityBuildCommand = constructors.getConstructorTakingEntity().apply(new UnsavableEntity());

        Method getRepositorySupplierMethod = ReflectionUtils.findMethod(DefaultBuildCommand.class, "getRepositorySupplier");
        assertNotNull(getRepositorySupplierMethod);
        ReflectionUtils.makeAccessible(getRepositorySupplierMethod);


        InvocationHandler handler = Proxy.getInvocationHandler(unsavableEntityBuildCommand);
        assertNotNull(handler.invoke(unsavableEntityBuildCommand, getRepositorySupplierMethod, new Object[0]));

        // There is no repository for 'UnsavableEntity'. The call to getRepository() should return null and remove the repository lookup function from this instance of UnsavableEntityBuildCommand
        assertNull(unsavableEntityBuildCommand.getRepository());
        assertNull(handler.invoke(unsavableEntityBuildCommand, getRepositorySupplierMethod, new Object[0]));

        // Even without lookup function it should still return null.
        assertNull(unsavableEntityBuildCommand.getRepository());
    }

    @Test
    @SuppressWarnings({ "unchecked", "ConstantConditions" })
    public void generateRepositorySupplier_multipleRepositoriesAvailable_wrongRepositoryReferenced_shouldThrowErrorAndAbort() {
        WrongRepositoryPersonFixtures wrongRepositoryPersonFixtures = new WrongRepositoryPersonFixtures();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(wrongRepositoryPersonFixtures);

        try {
            wrongRepositoryPersonFixtures.base().create();
            fail("Expected MultipleRepositoriesExistException was not thrown");
        } catch (MultipleRepositoriesExistException e) {
            assertEquals("Multiple repositories of (or extending) the type [nl._42.heph.domain.PersonRepository] were found. Please specify the repository with the most specific type or remove the duplicate repository", e.getMessage());
        }
    }
}
