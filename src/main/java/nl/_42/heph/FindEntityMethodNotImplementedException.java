package nl._42.heph;

/**
 * Exception which is thrown when your BuildCommand interface does not implement the <code>findEntity(T entity)</code> method.
 * If you see this error, please implement the findEntity method in your BuildCommand interface.
 */
class FindEntityMethodNotImplementedException extends RuntimeException {

    FindEntityMethodNotImplementedException(String message) {
        super(message);
    }

}
