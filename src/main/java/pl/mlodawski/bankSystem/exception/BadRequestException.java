package pl.mlodawski.bankSystem.exception;

/**
 * An exception that represents a bad request error in the application.
 * Typically used to indicate that the client sent a request that cannot be processed due to invalid input or parameters.
 *
 * This exception is usually associated with HTTP status 400 (BAD_REQUEST) when used in a web context,
 * and is intended to be caught and handled explicitly, such as in an exception handler.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
