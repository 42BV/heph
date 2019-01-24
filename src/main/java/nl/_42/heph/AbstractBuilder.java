package nl._42.heph;

import static java.lang.String.format;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import io.beanmapper.BeanMapper;
import nl._42.heph.generation.BuildCommandAdvice;
import nl._42.heph.generation.BuildCommandPointcut;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.Repository;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StopWatch;

/**
 * <p>
 *     Every entity builder will have to extend from this class. It provides a number of basic
 *     operations for preparing testdata of the entity's type. Supported operations are:
 * </p>
 * <ul>
 *     <li><b>blank</b>; provides a completely empty BuildCommand for an entity. All fields
 *     must still be set. Cannot be created immediately.</li>
 *     <li><b>base</b>; provides a fully outfitted entity, which must be creatable
 *     immediately.</li>
 *     <li><b>update</b>; rewraps the entity in a BuildCommand, making its BuildCommand's
 *     methods available.</li>
 *     <li><b>copy</b>; tries to make a clean copy of the entity (ie, new instance) and reset
 *     its ID to null.</li>
 * </ul>
 * @param <T> the type of the entity which is wrapped in the BuildCommand
 * @param <BC> the type of the BuildCommand
 */
public abstract class AbstractBuilder<T extends Persistable, BC extends AbstractBuildCommand> {

    /** The set of BuilderConstructors for this Builder */
    private BuilderConstructors<T, BC> builderConstructors;

    /** The set of methods of {@link AbstractBuildCommand} to redirect to a custom implementation (if overridden only) */
    private static final List<Method> ABSTRACT_BUILD_COMMAND_METHODS = Arrays.asList(AbstractBuildCommand.class.getDeclaredMethods());

    /** BuildCommand value storage key for proxy generation */
    private static final String BC = "BC";

    private static final Logger logger = LoggerFactory.getLogger(AbstractBuilder.class);

    /**
     * BeanMapper is used when the copy function is invoked. It will attempt to make a clear
     * copy. Be aware that non-traditional getters/setters may hamper the working of the copy
     * method.
     */
    @Autowired
    private BeanMapper beanMapper;

    /**
     * ApplicationContext is used to automatically assign a repository when the constructors() function is invoked for the first time.
     */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Method which provides the three constructors; i) constructor for the BuildCommand
     * if copy/update is called, ii) constructor for the BuildCommand if blank/base is called
     * and iii) constructor for the entity.
     *
     * A set of builderConstructors is generated once and then stored in this class.
     * This happens in {@link #generateBuilderConstructors()}.
     * @return the instance containing the three constructor lambdas.
     */
    private BuilderConstructors<T, BC> constructors() {
        if (builderConstructors ==  null) {
            // Lazily generate builderConstructors the first time they're needed.
            builderConstructors = generateBuilderConstructors();
        }
        return builderConstructors;
    }

    /**
     * Returns the entity wrapped in a BuildCommand. All fields which are required for
     * a successful create must have been provided. All fields must be overridable by later
     * calls, therefore the extensive use of lambdas is recommended (see various Builder
     * examples)
     * @return BuildCommand wrapping the entity
     */
    public abstract BC base();

    /**
     * Creates a new entity
     * @return the new entity
     */
    private T newEntity() {
        return constructors().getEntityConstructor().get();
    }

    /**
     * Method that provides the entity wrapped in a BuildCommand. None of the fields will have
     * been set. The entity cannot usually be created immediately.
     * @return BuildCommand wrapping the entity
     */
    public BC blank() {
        return constructors().getConstructorTakingSupplier().apply(this::newEntity);
    }

    /**
     * Method that returns a BuildCommand wrapping the entity. When create is called, no
     * attempt will be made to look for an existing entity. If no change is made, this can
     * of course result in constraint violations. Be aware of this. This call is mainly
     * useful when a fixture can be used with a number of small modifications.
     * @param entity the entity that must be wrapped in a new BuildCommand
     * @return BuildCommand wrapping the entity that must be updated
     */
    public BC update(T entity) {
        return constructors().getConstructorTakingEntity().apply(entity);
    }

    /**
     * Takes an existing entity, copies it using BeanMapper (be aware that this presumes
     * normal getters/setters to be in place; if this is not the case, this will imperil
     * the use of copy), sets the ID field of the copied entity to null and provides it
     * wrapped in a new BuildCommand. Useful if an object has been customized and its
     * siblings need only small changes.
     * @param entity the entity that must be copied and wrapped in a new BuildCommand
     * @return BuildCommand wrapping the copied entity
     */
    public BC copy(T entity) {
        T copy = beanMapper.map(entity, constructors().getEntityConstructor().get());
        nullifyIdField(copy);
        return update(copy);
    }

    /**
     * Set the id field of the given entity to null.
     * @param entity entity
     */
    private void nullifyIdField(T entity) {
        Field field = findIdFieldInHierarchy(entity.getClass());
        field.setAccessible(true);
        try {
            field.set(entity, null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not modify id field. ", e);
        }
    }

    /**
     * Looks up the id field in the object hierarchy.
     * This is done because getDeclaredFields only
     * returns private fields of the exact class
     * you're calling it on, but the id field may
     * be arbitrary class levels deep. So we
     * recurse until we find it or if we've reached
     * Object.class and we know we're done.
     *
     * If it cannot be found in the hierarchy
     * we throw an exception.
     *
     * @param entityClass class to start looking
     * @return id field
     */
    private Field findIdFieldInHierarchy(Class<?> entityClass) {
        try {
            return entityClass.getDeclaredField("id");
        } catch (NoSuchFieldException e) {
            /*
             * If we don't find the field we look
             * further up the hierarchy, if we
             * didn't reach object yet.
             * If we did, we'll rethrow the exception.
             */
            Class<?> superClass = entityClass.getSuperclass();
            if (superClass != Object.class) {
                return findIdFieldInHierarchy(superClass);
            } else {
                throw new RuntimeException("Cannot find id field in the hierarchy.", e);
            }
        }
    }

    /**
     * Generates an instance of builderConstructors suitable for the custom implementation of this class and its BuildCommand.
     * The constructor set needs three methods, which do the following:
     * i) constructor for copy/update: Instantiates the buildCommand (using its no-args constructor) with the linked entity
     * ii) constructor for blank/base: Instantiates the buildCommand (using its no-args constructor) with the supplied entity
     * iii) constructor for the entity: Instantiates the entity (using its no-args constructor).
     * @return Builder constructor for the entity type and build command type of this class.
     */
    @SuppressWarnings("unchecked")
    private BuilderConstructors<T, BC> generateBuilderConstructors() {
        // We resolve the generic class types T and BC.
        Class<?>[] genericTypes = GenericTypeResolver.resolveTypeArguments(getClass(), AbstractBuilder.class);
        Assert.isTrue(genericTypes != null && genericTypes.length == 2, "The AbstractBuilder class must contain exactly two class-level generic types");

        Class<?> entityClass = genericTypes[0];
        Class<?> buildCommandClass = genericTypes[1];

        // Function 1: Returns a new BuildCommand from an Entity.
        Function<T, BC> directEntityBuilderFunction = (entity -> instantiateBuildCommand(buildCommandClass, entity));

        // Function 2: Returns a new BuildCommand from a supplied Entity.
        Function<Supplier<T>, BC> lazySupplyingEntityBuilderFunction = (entity -> instantiateBuildCommand(buildCommandClass, entity.get()));

        // Function 3: Returns a new Entity
        Supplier<T> entitySupplyingFunction = () -> (T) BeanUtils.instantiateClass(entityClass);

        return new BuilderConstructors<>(directEntityBuilderFunction, lazySupplyingEntityBuilderFunction, entitySupplyingFunction);
    }

    /**
     * Instantiates a BuildCommand given its class and an initial Entity
     * This is done by looking at the no-args constructor (either present in {@link AbstractBuildCommand} or overridden in your own BuildCommand)
     * @param buildCommandClass Class of the custom BuildCommand interface to instantiate
     * @param entity Entity to set within the "entity" field of the default BuildCommand implementation
     * @return Instantiated BuildCommand for the given Entity
     */
    @SuppressWarnings("unchecked")
    private BC instantiateBuildCommand(Class<?> buildCommandClass, T entity) {
        Object[] buildCommandReference = new Object[1];

        // We currently need two kinds of proxies to have a working BuildCommand:
        // The 1st proxy (CGLib) is required to intercept all methods of the DefaultBuildCommandClass itself and redirect overridden methods to the BuildCommand interface of the user.
        // The 2nd proxy (Spring AOP) is required to intercept the "with" methods of the custom BuildCommand interface created by the user.
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(DefaultBuildCommand.class);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            // If any of the methods in DefaultBuildCommand is part of AbstractBuildCommand interface and has been overridden in the user's implementation,
            // pass the call back to the AOP proxy instance so the actual implementation can be executed.
            for (Method m : ABSTRACT_BUILD_COMMAND_METHODS) {
                if (m.getName().equals(method.getName()) && m.getParameterCount() == method.getParameterCount() && m.getReturnType().equals(method.getReturnType())) {
                    Method implementation = ReflectionUtils.findMethod(buildCommandClass, method.getName(), method.getParameterTypes());

                    if (implementation != null && implementation.isDefault()) {
                        return ReflectionUtils.invokeMethod(implementation, buildCommandReference[0], args);
                    }
                }
            }

            return proxy.invokeSuper(obj, args);

        });

        Supplier<Repository<T, ? extends Serializable>> repositorySupplier = buildRepositorySupplier(buildCommandClass);
        DefaultBuildCommand defaultBuildCommand = (DefaultBuildCommand) enhancer.create(new Class[] {Persistable.class, Supplier.class}, new Object[] {entity, repositorySupplier});

        // At this stage, we have a reference buildCommand implementation which can forward calls to overridden methods in the user's implementation.
        // However, it is not yet backed by the user's implementation.
        // Below, we join it to the user's implementation, delegating all "with" methods to the "withValue" method of the base instance.
        BuildCommandPointcut pointcut = new BuildCommandPointcut();
        BuildCommandAdvice advice = new BuildCommandAdvice(defaultBuildCommand);
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, advice);

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTargetSource(new SingletonTargetSource(defaultBuildCommand));
        proxyFactory.addInterface(buildCommandClass);
        proxyFactory.addAdvisor(advisor);
        BC buildCommand = (BC) proxyFactory.getProxy();
        advice.setProxy(buildCommand);

        // We need to store a reference to this buildCommand to redirect overridden methods called directly in DefaultBuildCommand to the user's implementation.
        buildCommandReference[0] = buildCommand;

        return buildCommand;
    }

    /**
     * Constructs a {@link Supplier} which can return a JpaRepository for the given repository type in the buildCommand.
     * If a Repository is available, it is returned when retrieving the value.
     * If no Repository (or no Spring context) is available, the supplier function returns {@code null}.
     * @param buildCommandClass Class of the buildCommand. This contains the generic type of the repository we are going to retrieve.
     * @return Repository if it exists, or {@code null}
     */
    @SuppressWarnings("unchecked")
    private Supplier<Repository<T, ? extends Serializable>> buildRepositorySupplier(Class<?> buildCommandClass) {
        return () -> {
            if (applicationContext == null) {
                return null;
            }

            // In most cases, the "Repositories" class is used to obtain a repository.
            // However, you can have 2 repositories for the same entity: One in the production code, and another in the test code, containing a method to identify the uniqueness of the fixture.
            // In some cases, the Repositories class returns the wrong repository type, so by using applicationContext.getBean we ensure the correct repository type is returned.
            Class<?>[] buildCommandTypes = GenericTypeResolver.resolveTypeArguments(buildCommandClass, AbstractBuildCommand.class);
            Assert.isTrue(buildCommandTypes != null && buildCommandTypes.length == 2, "The buildCommand class must have 2 generic types");
            Class<?> repositoryType = buildCommandTypes[1];

            Repository<T, ? extends Serializable> repository = null;

            try {
                repository = (Repository<T, ? extends Serializable>) applicationContext.getBean(repositoryType);
            } catch (NoUniqueBeanDefinitionException e) {
                // Multiple repositories exist and a top-level repository was requested. In this case, the user needs to remove the duplicate repository or request the most-specific instance.
                throw new MultipleRepositoriesExistException(format("Multiple repositories of (or extending) the type [%s] were found. Please specify the repository with the most specific type or remove the duplicate repository", repositoryType.getName()));
            } catch (NoSuchBeanDefinitionException ignored) {
                if (repositoryType != NoOpBeanSaver.class) {
                    logger.info("Repository of the type [{}] could not be instantiated, continuing without persistence support...", repositoryType.getName());
                }
                // This should rarely happen if not using the NoOpBeanSaver, but it is possible if the repository is for example annotated with @NoRepositoryBean.
            }

            return repository;
        };
    }
}
