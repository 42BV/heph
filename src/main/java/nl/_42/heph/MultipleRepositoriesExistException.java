package nl._42.heph;

/**
 * Exception which is thrown when multiple repositories of the same class type exist.
 * In this case, either specify the most specific type in your buildCommand (e.g. if PersonFixturesRepository extends from PersonRepository, then use PersonFixturesRepository)
 * If that is not the case, remove the duplicate repository from your project.
 */
class MultipleRepositoriesExistException extends RuntimeException {

    MultipleRepositoriesExistException(String message) {
        super(message);
    }
}
