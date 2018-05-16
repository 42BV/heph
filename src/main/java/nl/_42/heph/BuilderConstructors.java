package nl._42.heph;

import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.data.domain.Persistable;

/**
 * <p>
 *     Supplies a set of constructors for the AbstractBuilder to use. The following
 *     three constructors are expected:
 * </p>
 * <ul>
 *     <li><b>BuildCommand constructor with Entity</b>; the constructor for the BuildCommand
 *     used by the copy and update calls</li>
 *     <li><b>Buildcommand constructor with Entity Supplier</b> the constructor for the BuildCommand
 *     used by the base call</li>
 *     <li><b>Entity constructor</b>; the constructor for the entity itself</li>
 * </ul>
 * @param <T> type of the entity, must a be Persistable
 * @param <BC> type of the BuildCommand that will be an inner class of the Builder
 */
public class BuilderConstructors<T extends Persistable, BC extends AbstractBuildCommand> {

    /** the constructor for the BuildCommand used by the copy and update calls */
    private final Function<T, BC> constructorTakingEntity;
    /** the constructor for the BuildCommand used by the base call */
    private final Function<Supplier<T>, BC> constructorTakingSupplier;
    /** the constructor for the entity itself */
    private final Supplier<T> entityConstructor;

    /**
     * During construction, all functions are passed. The AbstractBuilder uses these
     * settings to create the BuildCommand its Entity.
     * @param constructorTakingEntity BuildCommand constructor Function taking the entity
     * @param constructorTakingSupplier BuildCommand constructor Function taking the entity Supplier
     * @param entityConstructor Entity constructor
     */
    public BuilderConstructors(
            Function<T, BC> constructorTakingEntity,
            Function<Supplier<T>, BC> constructorTakingSupplier,
            Supplier<T> entityConstructor) {
        this.constructorTakingEntity = constructorTakingEntity;
        this.constructorTakingSupplier = constructorTakingSupplier;
        this.entityConstructor = entityConstructor;
    }

    Function<T, BC> getConstructorTakingEntity() {
        return constructorTakingEntity;
    }

    Function<Supplier<T>, BC> getConstructorTakingSupplier() {
        return constructorTakingSupplier;
    }

    Supplier<T> getEntityConstructor() {
        return entityConstructor;
    }
}
