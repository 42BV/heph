package nl._42.heph.builder;

import java.util.function.Consumer;
import java.util.function.Supplier;

import nl._42.heph.AbstractBuildCommand;
import nl._42.heph.domain.Organization;
import nl._42.heph.domain.Person;
import nl._42.heph.domain.Workspace;
import nl._42.heph.lazy.EntityField;
import nl._42.heph.lazy.EntityId;

public interface PersonBuildCommand extends AbstractBuildCommand<Person, PersonFixturesRepository> {

    String CALLBACK_FUNCTION_TAG = "personCallback";

    @Override
    default Person findEntity(Person entity) {
        return getRepository().findByNameAndOrganizationIdAndWorkspaceId(entity.getName(), entity.getOrganizationId(), entity.getWorkspaceId());
    }

    @Override
    default void postProcess(Person person) {
        Consumer<Person> callbackFunction = getValue(CALLBACK_FUNCTION_TAG);

        if (callbackFunction != null) {
            callbackFunction.accept(person);
        }
    }

    PersonBuildCommand withName(String name);

    PersonBuildCommand withOrganization(Supplier<Organization> organizationReference);

    @EntityField("workspaceId")
    @EntityId
    PersonBuildCommand withWorkspace(Supplier<Workspace> workspaceReference);

    default PersonBuildCommand withCallbackFunction(Consumer<Person> callbackFunction) {
        putValue(CALLBACK_FUNCTION_TAG, callbackFunction);
        return this;
    }
}
