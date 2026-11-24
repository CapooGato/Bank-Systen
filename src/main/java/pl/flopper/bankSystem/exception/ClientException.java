package pl.flopper.bankSystem.exception;

/**
 * A custom exception that represents client-side errors in the application.
 *
 * This exception is typically used to indicate issues that arise from invalid
 * input, unauthorized access, or other client-related problems. It extends
 * {@code RuntimeException}, allowing it to be used as an unchecked exception.
 *
 * The {@code ClientException} is intended to signal conditions where the client
 * is at fault, and when thrown, it can be handled by dedicated handlers in
 * the application, such as displaying an error message to the user.
 */
public class ClientException extends RuntimeException {
    public ClientException(String message) {
        super(message);
    }
}
