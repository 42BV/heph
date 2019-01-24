package nl._42.heph.lazy;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Allows assigning the value passed in the 'with' method to a field with a different target name.
 * This can, for example, be useful if your entity has a field with an ID type.
 * The field may then  be named 'somethingId' instead of 'something', and with this annotation your builder can then have the name 'withSomething()'.
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface EntityField {

    /**
     * Literal name of the field in the entity.
     * @return Name of the field to set in the entity.
     */
    String value();
}
