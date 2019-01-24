package nl._42.heph.lazy;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation which indicates that the database ID of the passed object needs to be placed in the destination field, rather than the object itself.
 * This allows extraction of the database ID for all objects implementing (see {@link org.springframework.data.domain.Persistable} (any generic type).
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface EntityId {
}
