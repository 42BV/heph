package nl._42.heph;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.data.domain.Persistable;

/**
 * When the entity is referred to as a normal entity (as opposed to a Long ID), this
 * class is used. If the getter supplier shows the current value is null, the entity
 * reference will be resolved and set using the the setter consumer.
 * @param <T> classtype of the entity that is being resolved
 */
public class LazyEntityReference<T extends Persistable> extends AbstractLazyEntity<T,T>{

    public LazyEntityReference(Supplier<T> getter, Consumer<T> setter, Supplier<T> reference) {
        super(getter, setter, reference);
    }

    @Override
    public T convertEntity(T entity) {
        return entity;
    }

}
