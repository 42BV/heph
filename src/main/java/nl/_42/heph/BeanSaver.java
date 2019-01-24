package nl._42.heph;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

/**
 * A BeanSaver provides a custom way of saving an entity.
 * This is useful if you want to build an entity for which no Spring repository is available.
 * You may specify how to handle saving of this entity in your implementation.
 * If the entity cannot be persisted at all, use {@link NoOpBeanSaver} as repository type in your BuildCommand.
 * Note: This class extends from Spring's repository class to allow usage of bounded generics in {@link AbstractBuildCommand}
 * @param <T> Entity Type
 * @param <ID> Identifier type of the entity
 */
@NoRepositoryBean // This is not a Spring Repository
public interface BeanSaver<T, ID> extends Repository<T, ID> {

    /**
     * Persists the passed entity and returns it
     * @param entity Entity to persist
     * @return Persisted entity
     */
    T save(T entity);
}


