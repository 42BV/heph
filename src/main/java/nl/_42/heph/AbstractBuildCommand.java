package nl._42.heph;

import java.io.Serializable;
import java.util.function.Supplier;

import nl._42.heph.lazy.LazyEntity;

import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.support.Repositories;

/**
 * The {@link AbstractBuildCommand} interface serves as the base interface for building your fixtures.
 * Create your own BuildCommand interface and extend it from this interface.
 *
 * Override the method {@code findEntity(T entity)} to supply an unique method of obtaining your entity from the repository.
 * This method is called to determine if your fixture has already been built, or that a new instance needs to be created.
 *
 * Example implementation:
 * <code>
 *     default Person findEntity(Person entity) {
 *         return getRepository().findByName(entity.getName());
 *     }
 * </code>
 *
 * Once you've implemented the findEntity() method, just add interface methods to allow setting values within your entity.
 * These interface methods can be defined using the "with" syntax.
 * For example: If you want to update the String field "firstName",
 * create the following method:
 *
 * <code>PersonBuildCommand withFirstName(String firstName);</code>
 *
 * You can also create a default implementation of such methods to introduce custom behaviour. An example of this is:
 * <code>
 *     default PersonBuildCommand withFirstName(String firstName) {
 *         if (firstName == null) {
 *             throw new IllegalArgumentException("The first name of a Person can not be null!");
 *         }
 *         getInternalEntity().setFirstName(firstName);
 *         onFirstNameSetListener.onFirstNameSet(firstName);
 *         return this;
 *     }
 * </code>
 * @param <T> Type of the entity to build
 * @param <R> Class of the repository for the given entity.
 */
public interface AbstractBuildCommand<T extends Persistable, R extends Repository<? extends Persistable, ? extends Serializable>> {

    /**
     * The extending class must supply a method that looks up an existing entity.
     * For every BuildCommand a notion must be introduced of unicity of an entity
     * in the context of tests. If, for whatever reason, this is not possible, this
     * method just has to implement a <code>return null</code>. In that case, no
     * reuse takes place.
     * @param entity the entity to look for
     * @return the pre-existing entity if found, or else null
     */
    T findEntity(T entity);

    /**
     * Exposes the internal entity. Note that this method is used by the
     * extending class to set values on the encapsulated entity.
     * @return the encapsulated entity
     */
    T getInternalEntity();

    /**
     * References added with this method are resolved before the find() method is called. This
     * effectively means that the entity can be part of a search query. Note that new
     * references will be added to the head of the list, in order to force its execution before
     * pre-existing references.
     * @param lazyEntity the reference which needs to be resolved before a find
     */
    void addBeforeFindReference(LazyEntity lazyEntity);

    /**
     * References added with this method are resolved just before the construct/create. This
     * effectively means that the entities are created after the a find has been executed and
     * only when that find did not turn up a pre-existing entity. Note that new references will
     * be added to the head of the list, in order to force its execution before pre-existing
     * references.
     * @param lazyEntity the reference which needs to be resolved before a construct/create
     */
    void addBeforeCreateReference(LazyEntity lazyEntity);

    /**
     * Provides the repository for the Entity.
     * By default, this is the repository registered to Spring (see {@link Repositories#getRepositoryFor(Class)}).
     * An extending class may override this method to set a custom repository for the Entity
     * @return the repository for the entity
     */
    R getRepository();

    /**
     * Method that may be overridden by an extending class to supply extra logic BEFORE
     * saving/constructing the encapsulated entity. It is not required to override this
     * method.
     * @param entity the entity that is about to be constructed/saved
     */
    void preProcess(T entity);

    /**
     * Method that may be overridden by an extending class to supply extra logic AFTER
     * saving/constructing the encapsulated entity. It is not required to override this
     * method. A typical use-case for this method is when other entities must be created
     * as part of this creation, but not directly tied to it.
     * @param entity the entity that has just been constructed/saved
     */
    void postProcess(T entity);

    /**
     * Takes care to see if a pre-existing entity exists. If it does, it will be
     * returned, instead of a new one being created. Resolves all the before-find
     * references to supply the find with the right parameters. When an entity
     * has been rewrapped as a BuildCommand, the find will not be executed. Neither
     * will the find be executed if no repository exists.
     * @return the pre-existing entity if found, or else null
     */
    T find();

    /**
     * Basically only resolves the references and calls the pre-/post-process methods.
     * Returns the entity as-is. No find or save takes place.
     * @return wrapped entity with resolved references.
     */
    T construct();

    /**
     * Call that tries to find pre-existing entities that match its unique traits and
     * save the entity. Also works if no repository is present. In that case, it will
     * basically behave as <code>construct</code> does.
     * @return the created entity
     */
    T create();

    /**
     * Obtain a temporarily-stored value from within the BuildCommand instance.
     * This can be used - for example - to store a callback function or a list of required values.
     * @param tag Unique identifier of the value in the store. This is used to return the correct value
     * @param <V> Type of the returned value
     * @return Value if stored, or <code>null</code> if no value is present.
     */
    <V> V getValue(String tag);

    /**
     * Obtain a temporarily-stored value from within the BuildCommand instance.
     * This can be used - for example - to store a callback function or a list of required values.
     * @param tag Unique identifier of the value in the store. This is used to return the correct value
     * @param defaultValue Supplies a value to return when no value with this tag is present
     * @param <V> Type of the returned value
     * @return Value if stored, or 'defaultValue' if no value is present.
     */
    <V> V getValue(String tag, Supplier<V> defaultValue);

    /**
     * Store a value in this builder's BuildCommand instance.
     * This can be useful to keep a call back function or a separate list of objects which needs to be maintained.
     * @param tag Unique identifier to store the value with. Is used to later retrieve the value with.
     * @param value Value to store
     */
    void putValue(String tag, Object value);

}