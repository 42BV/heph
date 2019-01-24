package nl._42.heph;

import org.springframework.data.domain.Persistable;

/**
 * No-op implementation of {@link BeanSaver}. Returns the passed entity as-is.
 */
public class NoOpBeanSaver implements BeanSaver<Persistable, Long> {

    @Override
    public Persistable save(Persistable entity) {
        return entity;
    }
}
