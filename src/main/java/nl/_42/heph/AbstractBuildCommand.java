package nl._42.heph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * <p>
 *     The AbstractBuildCommand is supposed to be extended as an inner class of a Builder.
 *     The ABC takes care of managing the state of the encapsulated entity. Its primary
 *     function is to with the withX methods, which allow its values to be changed. The
 *     primary way of extracting entities from the BuildCommand is by calling
 *     <code>construct</code> or <code>create</code>. The methods are explained in detail
 *     below. In short, <code>construct</code> returns the entity in its pure form, whereas
 *     <code>create</code> makes uses find/save logic from the repository.
 * </p>
 * <p>
 *     The BuildCommand takes care of a number of other things as well:
 * </p>
 * <ul>
 *     <li>resolving entity references before a find</li>
 *     <li>resolving entity references before a create</li>
 *     <li>on creating, verifying whether a save can be done and, if not, just constructing</li>
 *     <li>ability to deal in and outside of Spring context (and its repositories)</li>
 *     <li>forces the extending class to supply a link to the Entity repository</li>
 *     <li>forces the extending class to supply a method to find an existing entity with the same unique properties</li>
 *     <li>allows the extending class to supply extra logic before processing</li>
 *     <li>allows the extending class to supply extra logic after processing (eg, creating other, related entities)</li>
 *     <li>rewrapping existing entities so that values can be changed through the BuildCommand</li>
 * </ul>
 * <p>
 *     The AbstractBuildCommand subscribes to the following lifecycle for a <b>create</b> call:
 * </p>
 * <ul>
 *     <li><b>Wrapping the entity</b>; triggered by calling the constructor, either
 *     with a new entity, or a pre-existing one</li>
 *     <li><b>Changing values</b>; done by calling the withX methods. Within Builders
 *     these can be either direct values or references. In tests, these are typically
 *     values only, not references.</li>
 *     <li><b>Triggering Creation</b>; triggered by calling construct() or create().
 *     This results in the execution of the creation steps</li>
 *     <li><b>Resolving before-find references</b>; all entity references which have been registered
 *     with addBeforeFindReference, will be resolved.</li>
 *     <li><b>Checking if the entity already exists</b>; if the entity already exists in the database,
 *     this entity will be returned. If not, the next step is executed.</li>
 *     <li><b>Resolving before-create references</b>; all entity references which have been registered
 *     with addBeforeCreateReference, will be resolved.</li>
 *     <li><b>Call preProcess</b>; logic of the extending class in the preProcess method will
 *     now be called.</li>
 *     <li><b>Creation</b>; if a repository is available, its save method will be called with
 *     the entity as an argument. If no repository is available, the entity is returned as-is.</li>
 *     <li><b>Call postProcess</b>; logic of the extending class in the postProcess method will
 *     now be called.</li>
 *     <li><b>Return the entity</b></li>
 * </ul>
 * <p>
 *     For the <b>construct</b> call, the constructing logic is the, except that i) no find
 *     is taking place and ii) the save on the repository is not called.
 * </p>
 * @param <T> the Entity which this BuildCommand represents
 */
public abstract class AbstractBuildCommand<T extends Persistable> {

    /** the entity which is wrapped by the BuildCommand */
    private final T entity;
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
    private final boolean updating;

    /**
     * Creates the BuildCommand by wrapping the entity. The entity supplied
     * to it can either be newly created or an existing entity (either save
     * or not).
     * @param entity the entity which must be represented by the BuildCommand
     */
    public AbstractBuildCommand(T entity) {
        this.entity = entity;
        this.updating = !entity.isNew();
    }

    /**
     * Creates the BuildCommand by passing the supplier for a new entity.
     * @param entity supplier which creates a new, unsaved entity
     */
    public AbstractBuildCommand(Supplier<T> entity) {
        this(entity.get());
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
     * An extending class must supply the repository for the Entity
     * @return the repository for the entity
     */
    protected abstract JpaRepository<T, ? extends Serializable> getRepository();

    /**
     * Method that may be overridden by an extending class to supply extra logic BEFORE
     * saving/constructing the encapsulated entity. It is not required to override this
     * method.
     * @param entity the entity that is about to be constructed/saved
     */
    protected void preProcess(T entity) {
    }

    /**
     * Method that may be overridden by an extending class to supply extra logic AFTER
     * saving/constructing the encapsulated entity. It is not required to override this
     * method. A typical use-case for this method is when other entities must be created
     * as part of this creation, but not directly tied to it.
     * @param entity the entity that has just been constructed/saved
     */
    protected void postProcess(T entity) {
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
     * @param entity the entity to look for
     * @return the pre-existing entity if found, or else null
     */
    protected abstract T findEntity(T entity);

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
    private T save() {
        return performPostProcessing(getRepository().save(performPreProcessing(entity)));
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

}
