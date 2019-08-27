package nl._42.heph;

import static java.lang.String.format;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import nl._42.heph.lazy.EntityField;
import nl._42.heph.lazy.EntityId;
import nl._42.heph.lazy.LazyEntity;
import nl._42.heph.lazy.LazyEntityId;
import nl._42.heph.lazy.LazyEntityReference;
import nl._42.heph.lazy.Resolve;
import nl._42.heph.lazy.ResolveStrategy;

import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.util.ReflectionUtils;

/**
 * DefaultBuildCommand serves as the backing implementation for all created BuildCommands.
 * It contains the logic to update and persist the entity which is currently being built.
 * @param <T> Type of the entity to build
 * @param <R> Repository class of the repository for the entity.
 */
public class DefaultBuildCommand<T extends Persistable, R extends Repository<T, ? extends Serializable>> implements AbstractBuildCommand<T, R> {

    /** the entity which is wrapped by the BuildCommand */
    private T entity;

    /** This field contains a store for maintaining temporary values required during the building of the entity */
    private final Map<String, Object> storedValues = new ConcurrentHashMap<>();

    /**
     * This field contains a supplier function to retrieve a default repository for this BuildCommand.
     * The reason a supplier function is used is that we only want to retrieve a default repository if the {@link #getRepository()} method was not overridden.
     */
    private Supplier<R> repositorySupplier;

    /** Once we have looked up the default repository, we store it here to prevent having to lookup the repository every time it is accessed */
    private R repository;

    /**
     * all the references which need to be resolved BEFORE a findEntity is executed, eg
     * when part of said method's parameters. Ie, early resolution.
     */
    private final List<LazyEntity> executeBeforeFind = new ArrayList<>();
    /** all the references which are solved right before creation/construction (ie, late) */
    private final List<LazyEntity> executeBeforeCreate = new ArrayList<>();
    /**
     * if true, states that no attempt will be made to find an already existing entity.
     * This mode is enabled when a copy or update has been called.
     */
    private boolean updating;

    /**
     * Creates the BuildCommand by wrapping the entity and storing a function to retrieve the repository. The entity supplied
     * to it can either be newly created or an existing entity (either save or not).
     * @param entity the entity which must be represented by the BuildCommand
     * @param repositorySupplier Function which can return a {@link JpaRepository} for the entity.
     */
    public DefaultBuildCommand(T entity, Supplier<R> repositorySupplier) {
        this.entity = entity;
        this.updating = !entity.isNew();
        this.repositorySupplier = repositorySupplier;
    }

    /**
     * Exposes the internal entity. Note that this method is used by the
     * extending class to set values on the encapsulated entity.
     * @return the encapsulated entity
     */
    public T getInternalEntity() {
        return entity;
    }

    /**
     * This method is called right before a find call is placed. The reason for
     * this method existing, is that some find logic requires entity references
     * to be resolved. Eg, when looking for an Employee, you would want the
     * Person to be available for searching. This would be the ideal candidate
     * for a before-find-reference.
     */
    private void resolveBeforeFindReferences() {
        resolveReferences(executeBeforeFind);
    }

    /**
     * This method is called after a find failed to turn up a pre-existing
     * entity. It will be called right before the entity is created, either by
     * a create or construct call.
     */
    private void resolveBeforeCreateReferences() {
        resolveReferences(executeBeforeCreate);
    }

    /**
     * Helper method for resolving references. It takes care both of the
     * before-find and the before-create batches. After references have
     * been resolved, the list will be emptied, to prevent accidental reruns.
     * @param references the references that need to be resolved.
     */
    private void resolveReferences(List<LazyEntity> references) {
        references.forEach(LazyEntity::resolve);
        references.clear();
    }

    /**
     * References added with this method are resolved before the find() method is called. This
     * effectively means that the entity can be part of a search query. Note that new
     * references will be added to the head of the list, in order to force its execution before
     * pre-existing references.
     * @param lazyEntity the reference which needs to be resolved before a find
     */
    public void addBeforeFindReference(LazyEntity lazyEntity) {
        executeBeforeFind.add(0, lazyEntity);
    }

    /**
     * References added with this method are resolved just before the construct/create. This
     * effectively means that the entities are created after the a find has been executed and
     * only when that find did not turn up a pre-existing entity. Note that new references will
     * be added to the head of the list, in order to force its execution before pre-existing
     * references.
     * @param lazyEntity the reference which needs to be resolved before a construct/create
     */
    public void addBeforeCreateReference(LazyEntity lazyEntity) {
        executeBeforeCreate.add(0, lazyEntity);
    }

    /**
     * Provides the repository for the Entity.
     * By default, this is the repository registered to Spring (see {@link Repositories#getRepositoryFor(Class)}).
     * An extending class may override this method to set a custom repository for the Entity
     * @return the repository for the entity
     */
    public R getRepository() {
        if (repository == null && getRepositorySupplier() != null) {
            repository = getRepositorySupplier().get();
            repositorySupplier = null; // Remove the supplier function to prevent it getting called multiple times when requesting a repository while there is none.
        }

        return repository;
    }

    /**
     * Method that may be overridden by an extending class to supply extra logic BEFORE
     * saving/constructing the encapsulated entity. It is not required to override this
     * method.
     * @param entity the entity that is about to be constructed/saved
     */
    public void preProcess(T entity) {
    }

    /**
     * Method that may be overridden by an extending class to supply extra logic AFTER
     * saving/constructing the encapsulated entity. It is not required to override this
     * method. A typical use-case for this method is when other entities must be created
     * as part of this creation, but not directly tied to it.
     * @param entity the entity that has just been constructed/saved
     */
    public void postProcess(T entity) {
    }

    /**
     * Called when the find did not turn a pre-existing entity. As part of its
     * logic, it takes care of calling all references required for creation. Also,
     * it calls the preProcess, which is empty by default, buy may be overridden
     * by extending classes.
     * @param entity the entity on which pre-processing must be performed
     * @return the pre-processed entity
     */
    private T performPreProcessing(T entity) {
        resolveBeforeCreateReferences();
        preProcess(entity);
        return entity;
    }

    /**
     * Called after the construct/save has taken place. As part of its logic, it
     * calls the postProcess, which is empty by default, buy may be overridden
     * by extending classes.
     * @param entity the entity which has just been constructed/saved
     * @return the post-processed entity
     */
    private T performPostProcessing(T entity) {
        postProcess(entity);
        return entity;
    }

    /**
     * The extending class must supply a method that looks up an existing entity.
     * For every BuildCommand a notion must be introduced of unicity of an entity
     * in the context of tests. If, for whatever reason, this is not possible, this
     * method just has to implement a <code>return null</code>. In that case, no
     * reuse takes place.
     * Since you are obliged to override this method, the default implementation will throw an {@link FindEntityMethodNotImplementedException}.
     * @param entity the entity to look for
     * @return the pre-existing entity if found, or else null
     */
    public T findEntity(T entity) {
        throw new FindEntityMethodNotImplementedException(
                "Please override the 'findEntity()' method in your BuildCommand interface by using a default implementation.");
    }

    /**
     * Takes care to see if a pre-existing entity exists. If it does, it will be
     * returned, instead of a new one being created. Resolves all the before-find
     * references to supply the find with the right parameters. When an entity
     * has been rewrapped as a BuildCommand, the find will not be executed. Neither
     * will the find be executed if no repository exists.
     * @return the pre-existing entity if found, or else null
     */
    public T find() {
        resolveBeforeFindReferences();
        if (updating) {
            return null;
        }
        if (getRepository() == null) {
            return null;
        }
        return findEntity(getInternalEntity());
    }

    /**
     * Used internally by the <code>create</code> in case no repository exists.
     * Calls both pre-/post-process methods.
     * @return wrapped entity
     */
    private T internalConstruct() {
        return performPostProcessing(performPreProcessing(entity));
    }

    /**
     * Saves the entity using its repository.
     * @return the saved entity, ie with an ID
     */
    @SuppressWarnings("unchecked")
    private T save() {
        T preProcessed = performPreProcessing(entity);
        T saved = entity;

        R repository = getRepository();
        if (repository instanceof CrudRepository) {
            saved = ((CrudRepository<T, ?>) repository).save(preProcessed);
        } else if (repository instanceof BeanSaver) {
            saved = ((BeanSaver<T, ?>) repository).save(preProcessed);
        }

        return performPostProcessing(saved);
    }

    /**
     * Determines whether to use <code>save</code> if a repository is present, or
     * <code>construct</code> if not.
     * @return the constructed or saved entity
     */
    private T constructOrSave() {
        return getRepository() == null ? internalConstruct() : save();
    }

    /**
     * Basically only resolves the references and calls the pre-/post-process methods.
     * Returns the entity as-is. No find or save takes place.
     * @return wrapped entity with resolved references.
     */
    public T construct() {
        resolveBeforeFindReferences();
        return internalConstruct();
    }

    /**
     * Call that tries to find pre-existing entities that match its unique traits and
     * save the entity. Also works if no repository is present. In that case, it will
     * basically behave as <code>construct</code> does.
     * @return the created entity
     */
    public T create() {
        T entity = find();
        if (entity == null) {
            entity = constructOrSave();
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    public <V> V getValue(String tag) {
        return (V) storedValues.get(tag);
    }

    @SuppressWarnings("unchecked")
    public <V> V getValue(String tag, Supplier<V> defaultValue) {
        return (V) storedValues.computeIfAbsent(tag, (__) -> defaultValue.get());
    }

    public void putValue(String tag, Object value) {
        storedValues.put(tag, value);
    }

    /**
     * Applies a dynamic value to the entity stored within this instance.
     * @param fieldName Name of the field to set
     * @param fieldValue Value to set in the field
     * @param resolveAnnotation Optional {@link Resolve} annotation to specify behaviour of resolving {@link LazyEntity}.
     * @param entityFieldAnnotation Optional {@link EntityField} annotation to override the name of the destination field to use.
     * @param entityIdAnnotation Optional {@link EntityId} annotation to specify that the database ID of the fieldValue must be used instead of the actual object.
     * @return Current instance of the buildCommand, with the value applied to the builder's entity.
     */
    public AbstractBuildCommand<T, R> withValue(String fieldName, Object fieldValue, Resolve resolveAnnotation, EntityField entityFieldAnnotation,
            EntityId entityIdAnnotation) {
        Field field = getAccessibleField(fieldName, entityFieldAnnotation);

        if (fieldValue != null && fieldValue.getClass().isArray()) {
            return setArrayValue(field, fieldValue);
        } else if (fieldValue instanceof Collection) {
            return setCollectionValue(field, (Collection) fieldValue);
        } else if (fieldValue instanceof Supplier) {
            return handleSuppliedValue(field, (Supplier) fieldValue, resolveAnnotation, entityIdAnnotation);
        } else {
            return setOtherValue(field, fieldValue, entityIdAnnotation);
        }
    }

    /**
     * Places a value delivered as Array in the entity. The destination field in the Entity can either be an array or a Collection.
     * If the destination field is null, it will be initialized with the supplied values.
     * If the destination field is not null, the supplied values will be added to the already-existing values. This is handy to add multiple values to a
     * buildable entity just by repeatedly calling the corresponding "with" method.
     * @param field Entity field to populate
     * @param inputValues Values to copy to the destination field.
     * @return Current instance of the buildCommand, with the value applied to the builder's entity.
     */
    @SuppressWarnings("unchecked")
    private AbstractBuildCommand<T, R> setArrayValue(Field field, Object inputValues) {
        Class<?> targetType = field.getType();

        // Array -> Array
        if (targetType.isArray()) {
            // Primitive-type arrays are not an instance of Object[], so therefore we see the array as a regular Object.
            Object destinationArray = ReflectionUtils.getField(field, entity);

            if (destinationArray == null) {
                destinationArray = copyArray(inputValues, Array.getLength(inputValues));
                ReflectionUtils.setField(field, entity, destinationArray);
            } else {
                int originalLength = Array.getLength(destinationArray);
                int inputLength = Array.getLength(inputValues);
                destinationArray = copyArray(destinationArray,
                        originalLength + inputLength); // Increase the size of the new array to match current + new values in total.
                ReflectionUtils.setField(field, entity, destinationArray);
                //noinspection SuspiciousSystemArraycopy method can only be called with array class and destination will always be some sort of array.
                System.arraycopy(inputValues, 0, destinationArray, originalLength,
                        inputLength); // Native-copy the passed array to the end of the already-existing array.
            }
        } else if (Collection.class.isAssignableFrom(targetType)) { // Array -> Collection
            Collection current = (Collection) ReflectionUtils.getField(field, entity);

            if (current == null) {
                current = createCollection(targetType); // Create empty collection of the right type
                ReflectionUtils.setField(field, entity, current);
            }

            if (inputValues instanceof Object[]) {
                Collections.addAll(current, (Object[]) inputValues);
            } else {
                Collections.addAll(current, copyToNonPrimitiveArray(inputValues));
            }
        } else {
            throw new IllegalArgumentException(
                    format("Attempted to set array value into non-array / collection field [%s] of [%s]", field.getName(), entity.getClass().getName()));
        }

        return this;
    }

    /**
     * Places a value delivered as Collection in the entity. The destination field in the Entity can either be an array or a Collection.
     * If the destination field is null, it will be initialized with the supplied values.
     * If the destination field is not null, the supplied values will be added to the already-existing values. This is handy to add multiple values to a
     * buildable entity just by repeatedly calling the corresponding "with" method.
     * @param field Entity field to populate
     * @param inputValues Values to copy to the destination field.
     * @return Current instance of the buildCommand, with the value applied to the builder's entity.
     */
    @SuppressWarnings("unchecked")
    private AbstractBuildCommand<T, R> setCollectionValue(Field field, Collection inputValues) {
        Class<?> targetType = field.getType();

        // Collection -> Array
        if (targetType.isArray()) {
            setArrayValue(field, inputValues);
        } else if (Collection.class.isAssignableFrom(targetType)) { // Collection -> Collection
            Collection current = (Collection) ReflectionUtils.getField(field, entity);

            if (current == null) {
                current = createCollection(targetType); // Create empty collection of the right type
                ReflectionUtils.setField(field, entity, current);
            }

            current.addAll(inputValues);
        } else {
            throw new IllegalArgumentException(
                    format("Attempted to set collection value into non-array / collection field [%s] of [%s]", field.getName(), entity.getClass().getName()));
        }

        return this;
    }

    private void setArrayValue(Field field, Collection inputValues) {
        Object[] originalValues = (Object[]) ReflectionUtils.getField(field, entity);

        int arraySize = getArraySize(inputValues, originalValues);

        Object newArray = Array.newInstance(field.getType().getComponentType(), arraySize);

        int i = 0;
        //Add original array values
        if (originalValues != null) {
            for (Object object : originalValues) {
                Array.set(newArray, i, object);
                i++;
            }
        }

        //Add new values
        for (Object object : inputValues) {
            Array.set(newArray, i, object);
            i++;
        }

        ReflectionUtils.setField(field, entity, newArray);
    }

    private int getArraySize(Collection inputValues, Object[] destinationArray) {
        int arraySize = inputValues.size();
        if (destinationArray != null) {
            arraySize += destinationArray.length;
        }
        return arraySize;
    }

    /**
     * Handles the delivery of a Supplied value to the underlying Entity object. For this to work, we consider four cases:
     * - Process value before creation (destination is an entity)
     * - Process value before creation (destination is an identifier)
     * - Process value before lookup (destination is an entity)
     * - Process value before lookup (destination is an identifier)
     * @param field Field to place the value in (either points to a Hibernate-mapped object or to an identifier)
     * @param suppliedValue Value to place in the field (always an object)
     * @param resolveAnnotation Optional instruction to resolve the value either before lookup or before creation (default: before creation)
     * @param entityIdAnnotation Optional instruction to resolve only the ID of the value, not the value itself.
     * @return Current instance of the buildCommand, with the value applied to the builder's entity.
     */
    private AbstractBuildCommand<T, R> handleSuppliedValue(Field field, Supplier<?> suppliedValue, Resolve resolveAnnotation, EntityId entityIdAnnotation) {
        // A supplied value can be handled in 2 ways: before persisting (beforeCreate) or before initial lookup (beforeFind).
        // We use the Resolve annotation to determine this (default if absent: beforeCreate).
        ResolveStrategy strategy = ResolveStrategy.BEFORE_CREATE;

        if (resolveAnnotation != null) {
            strategy = resolveAnnotation.value();
        }

        switch (strategy) {
        case BEFORE_CREATE:
            addBeforeCreateReference(buildLazyEntity(field, suppliedValue, entityIdAnnotation));
            break;
        case BEFORE_FIND:
            addBeforeFindReference(buildLazyEntity(field, suppliedValue, entityIdAnnotation));
            break;
        }

        return this;
    }

    /**
     * Places a value which is not of type Array, Collection or Supplier in the entity.
     * This is done by simply copying the passed value to the field of the class.
     * @param field Field to place the value in
     * @param fieldValue New value to place in the field
     * @return Current instance of the buildCommand, with the value applied to the builder's entity.
     */
    private AbstractBuildCommand<T, R> setOtherValue(Field field, Object fieldValue, EntityId entityIdAnnotation) {
        if (entityIdAnnotation == null || !(fieldValue instanceof Persistable)) {
            ReflectionUtils.setField(field, entity, fieldValue);
        } else {
            ReflectionUtils.setField(field, entity, ((Persistable) fieldValue).getId());
        }

        return this;
    }

    /**
     * Returns an accessible field, which can always be modified
     * @param defaultFieldName Determined name of the field to obtain of the entity class
     * @param entityFieldAnnotation Optional annotation to specify a different name of the field to obtain
     * @return Accessible field for the given name. If a field with this name does not exist, an {@link IllegalArgumentException} is thrown.
     */
    private Field getAccessibleField(String defaultFieldName, EntityField entityFieldAnnotation) {
        String realFieldName;
        if (entityFieldAnnotation != null) {
            realFieldName = entityFieldAnnotation.value();
        } else {
            realFieldName = defaultFieldName;
        }

        Field field = ReflectionUtils.findField(entity.getClass(), realFieldName);

        if (field == null) {
            throw new IllegalArgumentException(format("Could not set value for entity class [%s]: Field [%s] is not present in the class or its superclasses!",
                    entity.getClass().getName(), realFieldName));
        }

        ReflectionUtils.makeAccessible(field);
        return field;
    }

    /**
     * Constructs a {@link LazyEntity} to pass a supplied value to the destination field.
     * This can either be a {@link LazyEntityReference}, or a {@link LazyEntityId}, depending on the entityId annotation being present.
     * @param destinationField Field to place the value in
     * @param suppliedValue Value to be placed in the field
     * @param entityIdAnnotation Optional annotation to indicate we want to return a {@link LazyEntityId}. The default is {@link LazyEntityReference}.
     * @return LazyEntity of the right type
     */
    @SuppressWarnings("unchecked")
    private LazyEntity buildLazyEntity(Field destinationField, Supplier<?> suppliedValue, EntityId entityIdAnnotation) {
        PropertyDescriptor pd;

        try {
            pd = new PropertyDescriptor(destinationField.getName(), entity.getClass());
        } catch (IntrospectionException e) {
            throw new IllegalStateException(format("Failed to apply lazy value to [%s]: ", entity.getClass().getName()), e);
        }

        // We look up the getter and the setter of the field, and pass these to the LazyEntity instance so it can safely set the values to the entity.
        Supplier<?> valueGetter = null;
        Consumer<?> valueSetter = null;

        // We look up the getter and the setter of the field, and pass these to the LazyEntity instance so it can safely set the values to the entity.
        if (pd.getReadMethod() != null) {
            valueGetter = () -> invokeOnEntityWithHandledExceptions(pd.getReadMethod(),
                    () -> "Failed to call method [%s] to get value from object of class [%s]");
        }

        if (pd.getWriteMethod() != null) {
            valueSetter = (value) -> invokeOnEntityWithHandledExceptions(pd.getWriteMethod(),
                    () -> "Failed to call method [%s] to apply value to object of class [%s]", value);
        }

        // If an entityId annotation is present, then we map the destination field to the ID of the passed object.
        if (entityIdAnnotation != null) {
            return new LazyEntityId<>((Supplier) valueGetter, valueSetter, (Supplier) suppliedValue);
        } else {
            //noinspection CastCanBeRemovedNarrowingVariableType -> Not possible, would supply incorrect type if entity has EntityId and destination field is not Persistable.
            return new LazyEntityReference<>((Supplier<Persistable>) valueGetter, (Consumer<Persistable>) valueSetter, (Supplier<Persistable>) suppliedValue);
        }
    }

    /**
     * Invokes the passed method on the entity. As this is a dangerous operation, it is required to specify a formatted exception message to explain what has gone wrong.
     * @param methodToInvoke Method to invoke on the entity class
     * @param exceptionMessage Exception message if the method cannot be invoked using the given parameters
     * @param args Parameters of the method.
     * @return Result of the invoked method, or {@code null} if it is a void method.
     */
    private Object invokeOnEntityWithHandledExceptions(Method methodToInvoke, Supplier<String> exceptionMessage, Object... args) {
        try {
            return methodToInvoke.invoke(entity, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalArgumentException(format(exceptionMessage.get(), methodToInvoke.getName(), entity.getClass().getName()), e);
        }
    }

    private Supplier<R> getRepositorySupplier() {
        return repositorySupplier;
    }

    private Collection createCollection(Class<?> collectionType) {
        if (List.class.isAssignableFrom(collectionType)) {
            return new ArrayList();
        }

        if (Set.class.isAssignableFrom(collectionType)) {
            return new HashSet();
        }

        if (Collection.class.isAssignableFrom(collectionType)) {
            return new ArrayList();
        }

        throw new IllegalArgumentException("Could not create new collection of type " + collectionType);
    }

    private Object[] copyToNonPrimitiveArray(Object primitiveArray) {
        int arrayLength = Array.getLength(primitiveArray);
        Object[] outputArray = new Object[arrayLength];

        for (int i = 0; i < arrayLength; ++i) {
            outputArray[i] = Array.get(primitiveArray, i);
        }

        return outputArray;
    }

    private Object copyArray(Object array, int length) {
        if (array instanceof byte[]) {
            return Arrays.copyOf((byte[]) array, length);
        } else if (array instanceof short[]) {
            return Arrays.copyOf((short[]) array, length);
        } else if (array instanceof int[]) {
            return Arrays.copyOf((int[]) array, length);
        } else if (array instanceof long[]) {
            return Arrays.copyOf((long[]) array, length);
        } else if (array instanceof float[]) {
            return Arrays.copyOf((float[]) array, length);
        } else if (array instanceof double[]) {
            return Arrays.copyOf((double[]) array, length);
        } else if (array instanceof boolean[]) {
            return Arrays.copyOf((boolean[]) array, length);
        } else if (array instanceof char[]) {
            return Arrays.copyOf((char[]) array, length);
        } else if (array instanceof Object[]) {
            return Arrays.copyOf((Object[]) array, length);
        }

        throw new IllegalArgumentException("Object is not an array.");
    }
}
