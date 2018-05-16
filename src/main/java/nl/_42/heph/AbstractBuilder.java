package nl._42.heph;

import io.beanmapper.BeanMapper;
import mockit.Deencapsulation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Persistable;

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

    /**
     * BeanMapper is used when the copy function is invoked. It will attempt to make a clear
     * copy. Be aware that non-traditional getters/setters may hamper the working of the copy
     * method.
     */
    @Autowired
    private BeanMapper beanMapper;

    /**
     * Method which must provide the three constructors; i) constructor for the BuildCommand
     * if copy/update is called, ii) constructor for the BuildCommand if blank/base is called
     * and iii) constructor for the entity.
     * @return the instance containing the three constructor lambdas.
     */
    public abstract BuilderConstructors<T, BC> constructors();

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
        Deencapsulation.setField(copy, "id", null);
        return update(copy);
    }

}
