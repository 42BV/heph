package nl._42.heph.shared;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import nl._42.heph.BeanSaver;

/**
 * "Repository" which stores all entities of a type T in a map (by ID)
 * Demonstrates the working of Heph with a non-Spring repository.
 * @param <T> Entity type to store in this class.
 */
public abstract class MockRepository<T extends AbstractEntity> implements BeanSaver<T, Long> {

    private final AtomicLong idSequence = new AtomicLong(0);
    private final Map<Long, T> entities = new HashMap<>();

    public void clear() {
        entities.clear();
    }

    public Collection<T> findAll() {
        return entities.values();
    }

    public T findOne(Long id) {
        return entities.get(id);
    }

    public T save(T entity) {
        // Add the ID to the entity if it is a new entity.
        if (entity.getId() == null) {
            entity.setId(idSequence.addAndGet(1));
        }

        // Place the entity in the repository
        entities.put(entity.getId(), entity);

        return entity;
    }

    public void delete(AbstractEntity entity) {
        entities.remove(entity.getId());
    }
}
