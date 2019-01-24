package nl._42.heph.lazy;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation which indicates when a supplied value needs to be resolved (e.g. <code>myBuildCommand withCustomer(Supplier&lt;Person&gt; customer);</code>).
 * This can be either before persistence ({@link ResolveStrategy#BEFORE_CREATE}) or before initial lookup ({@link ResolveStrategy#BEFORE_FIND})
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Resolve {

    /**
     * Determines when to resolve a lazy entity (before creation or before lookup).
     * @return When to resolve the entity.
     */
    ResolveStrategy value() default ResolveStrategy.BEFORE_CREATE;
}
