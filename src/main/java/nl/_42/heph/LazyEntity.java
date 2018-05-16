package nl._42.heph;

/**
 * Consists of a triplet with a getter, setter and supplier. The getter is called
 * to see if a value already exists. If it does, it must break of the resolution.
 * The supplier is used to fetch the Entity (either by reusing or creating it – that
 * is up to the BuildCommand). The setter is used to set the entity with.
 */
public interface LazyEntity {

    /**
     * resolves the reference by calling it. Execution must only take place
     * if the reference exists and the current entity value is null.
     */
    void resolve();

}
