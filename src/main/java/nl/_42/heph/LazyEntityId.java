package nl._42.heph;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.data.domain.Persistable;

/**
 * When the entity is referred to as a Long ID, this class is used. If the getter supplier
 * (getXId) shows the current value is null, the entity reference will be resolved, its ID
 * extracted and set using the the setter consumer (setXId).
 * @param <T> classtype of the entity that is being resolved
 * @param <A> type for the entities that are stored by ID, in this case Long
 */
public class LazyEntityId<T extends Persistable, A extends Long> extends AbstractLazyEntity<T,A> {

    public LazyEntityId(Supplier<A> getter, Consumer<A> setter, Supplier<T> reference) {
        super(getter, setter, reference);
    }

    /**
     * The entity's ID must be extracted. This value will be set using the setter consumer
     * @param entity the entity (of type T) to be converted to its A type (either same, or Long)
     * @return the ID of the entity, a Long
     */
    @Override
    public A convertEntity(T entity) {
        return (A)entity.getId();
    }

}
