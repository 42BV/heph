package nl._42.heph.lazy;

import nl._42.heph.AbstractBuildCommand;
import nl._42.heph.DefaultBuildCommand;

import org.springframework.data.domain.Persistable;

/**
 * Strategies specifying the moment to resolve a {@link LazyEntity}
 */
public enum ResolveStrategy {

    /**
     * Resolve the lazy entity before lookup in {@link AbstractBuildCommand#findEntity(Persistable)}
     */
    BEFORE_FIND,

    /**
     * Resolve the lazy entity before saving in {@link DefaultBuildCommand#save()}
     */
    BEFORE_CREATE
}
