package nl._42.heph;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.function.Supplier;

import io.beanmapper.BeanMapper;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Persistable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

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

    private BuilderConstructors<T, BC> defaultBuilderConstructors;

    /**
     * BeanMapper is used when the copy function is invoked. It will attempt to make a clear
     * copy. Be aware that non-traditional getters/setters may hamper the working of the copy
     * method.
     */
    @Autowired
    private BeanMapper beanMapper;

    /**
     * Method which provides the three constructors; i) constructor for the BuildCommand
     * if copy/update is called, ii) constructor for the BuildCommand if blank/base is called
     * and iii) constructor for the entity.
     *
     * If not overridden, a set of builderConstructors is generated automatically.
     * See {@link #generateDefaultBuilderConstructors()} for more info on builder construction generation.
     * @return the instance containing the three constructor lambdas.
     */
    public BuilderConstructors<T, BC> constructors() {
        if (defaultBuilderConstructors ==  null) {
            // Lazily generate builderConstructors the first time they're needed.
            defaultBuilderConstructors = generateDefaultBuilderConstructors();
        }
        return defaultBuilderConstructors;
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
     * Generates a default pair of builder constructors.
     * The constructor has three methods, which do the following:
     * i) constructor for copy/update: Instantiates the buildCommand (using its no-args constructor) with the linked entity
     * ii) constructor for blank/base: Instantiates the buildCommand (using its no-args constructor) with the supplied entity
     * iii) constructor for the entity: Instantiates the entity (using its no-args constructor).
     * @return Builder constructor for the entity type and build command type of this class.
     */
    @SuppressWarnings("unchecked")
    private BuilderConstructors<T, BC> generateDefaultBuilderConstructors() {
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
     * @param buildCommandClass Class of the BuildCommand to instantiate
     * @param entity Entity to set within the "entity" field
     * @return Instantiated BuildCommand for the given Entity
     */
    @SuppressWarnings("unchecked")
    private BC instantiateBuildCommand(Class<?> buildCommandClass, T entity) {
        BC buildCommand;

        try {
            // If the buildCommand is a separate class (that is: not an inner class), directly instantiate it.
            buildCommand = (BC) BeanUtils.instantiateClass(buildCommandClass);
        } catch (BeanInstantiationException e) {
            try {
                // The buildCommand can be an inner class. In that case, look for the constructor taking its outer class as parameter.
                Constructor innerClassConstructor = ReflectionUtils.accessibleConstructor(buildCommandClass, this.getClass());
                buildCommand = (BC) BeanUtils.instantiateClass(innerClassConstructor, this);
            } catch (NoSuchMethodException e2) {
                // If the buildCommand cannot be instantiated as regular class or as inner class, throw an exception.
                throw new BeanInstantiationException(buildCommandClass, "A no-args constructor is required!");
            }
        }

        // Once the command has been instantiated, place the entity and "updating" field inside it (this is what the overridable constructors also do).
        Field entityField = ReflectionUtils.findField(buildCommandClass, "entity");
        Field updatingField = ReflectionUtils.findField(buildCommandClass, "updating");
        Assert.isTrue(entityField != null, "BuildCommand class or its superclass does not contain the required field 'entity'");
        Assert.isTrue(updatingField != null, "BuildCommand class or its superclass does not contain the required field 'updating'");
        entityField.setAccessible(true);
        updatingField.setAccessible(true);

        ReflectionUtils.setField(entityField, buildCommand, entity);
        ReflectionUtils.setField(updatingField, buildCommand, !entity.isNew());

        return buildCommand;
    }
}
