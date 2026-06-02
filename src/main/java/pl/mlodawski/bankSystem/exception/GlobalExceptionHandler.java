package pl.mlodawski.bankSystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles exceptions of type ResourceNotFoundException and returns an appropriate
     * error view with the error details.
     *
     * @param ex the ResourceNotFoundException that was thrown
     * @param model the model to which error details are added
     * @return the name of the error view to render
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFoundException(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorTitle", "Resource Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    /**
     * Handles exceptions of type {@link BadRequestException} and returns a view displaying an error message.
     * Adds information about the error to the model to be displayed in the view.
     *
     * @param ex the exception that triggered this handler
     * @param model the model object to add attributes for the error page
     * @return the name of the error view to be rendered
     */
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequestException(BadRequestException ex, Model model) {
        model.addAttribute("errorTitle", "Bad Request");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    /**
     * Handles the IllegalArgumentException by setting an error message as a flash attribute
     * and redirecting the user to the "account" page.
     *
     * @param ex the IllegalArgumentException that was thrown
     * @param model the model to carry data to the view
     * @param redirectAttributes the RedirectAttributes used to add flash attributes for redirects
     * @return a redirect string to the "account" page
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException ex, Model model, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/account";
    }

    /**
     * Handles the InsufficientFundsException by setting an error message in the redirect attributes
     * and redirecting the user to the account page.
     *
     * @param ex the InsufficientFundsException that was thrown
     * @param redirectAttributes the RedirectAttributes object used to pass flash attributes
     * @return a redirect to the account page
     */
    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInsufficientFundsException(InsufficientFundsException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/account";
    }

    /**
     * Handles exceptions of type ClientException. This method sets the HTTP response status to
     * 400 (BAD_REQUEST) and updates the model with error details to be displayed on the error view.
     *
     * @param ex the ClientException that was thrown
     * @param model the Model object used to bind attributes for the view
     * @return the name of the error view to be rendered
     */
    @ExceptionHandler(ClientException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleClientException(ClientException ex, Model model) {
        model.addAttribute("errorTitle", "Client Error");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    /**
     * Handles exceptions of type {@code ServerException}.
     * Sets error details in the specified model and returns the name of the error view.
     *
     * @param ex    the exception that occurred, of type {@code ServerException}
     * @param model the model to which error details are added
     * @return the name of the view to display, in this case "error"
     */
    @ExceptionHandler(ServerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleServerException(ServerException ex, Model model) {
        model.addAttribute("errorTitle", "Server Error");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    /**
     * Handles generic exceptions that are not explicitly managed by other exception handlers in the application.
     * Adds attributes to the provided model with information about the error, including an error title and message.
     * Responds with an HTTP status of INTERNAL_SERVER_ERROR and returns a view named "error".
     *
     * @param ex    the exception that was not explicitly handled
     * @param model the model to which error details are added
     * @return the view name "error"
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        model.addAttribute("errorTitle", "Unexpected Error");
        model.addAttribute("errorMessage", "An unexpected error occurred: " + ex.getMessage());
        return "error";
    }
}