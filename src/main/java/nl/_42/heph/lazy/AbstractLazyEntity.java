package nl._42.heph.lazy;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.data.domain.Persistable;

/**
 * Class that takes care of shared logic for all LazyEntity implementations. The most
 * important part is the verifying whether a value is already stored and, if not,
 * dereferencing the entity and storing it
 * @param <T> the type of the entity
 * @param <A> the type of the value to store (can either be also the entity, or Long)
 */
public abstract class AbstractLazyEntity<T, A> implements LazyEntity {

    /**
     * the supplier that returns the currently set value. Used to determine if the
     * setting of the resolved reference will have to take place.
     * */
    private final Supplier<A> getter;
    /** the consumer of the resolved reference, used to set the value */
    private final Consumer<A> setter;
    /** the reference to the entity, will be set using the setter consumer */
    private final Supplier<T> reference;

    AbstractLazyEntity(Supplier<A> getter, Consumer<A> setter, Supplier<T> reference) {
        this.getter = getter;
        this.setter = setter;
        this.reference = reference;
    }

    /**
     * The resolution logic will verify whether the reference and getters are legit. If this
     * is the case and there is no existing value, the reference will be resolved and its
     * resulting value set using the setter consumer.
     */
    @Override
    public void resolve() {
        if (reference == null || (getter != null && getter.get() != null)) {
            return;
        }
        setter.accept(convertEntity(reference.get()));
    }

    /**
     * Converts the entity to its final form. If working with a LazyEntityReference, this will
     * also be an entity. If LazyEntityId, this will be a Long.
     * @param entity the entity (of type T) to be converted to its A type (either same, or Long)
     * @return the converted entity of type A
     */
    public abstract A convertEntity(T entity);

}
