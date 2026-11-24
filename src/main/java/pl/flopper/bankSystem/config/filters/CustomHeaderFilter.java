package pl.flopper.bankSystem.config.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomHeaderFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(CustomHeaderFilter.class);
    private static final String REQUIRED_HEADER_NAME = "X-API-KEY";
    private static final String REQUIRED_HEADER_VALUE = "secret-api-key-value";
    private static final String PROTECTED_ENDPOINT = "/api/filter/example1";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        logger.debug("Przetwarzanie ścieżki: {}", requestPath);

        if (requestPath.equals(PROTECTED_ENDPOINT)) {
            logger.info("Wykryto żądanie do chronionego endpointu: {}", requestPath);

            String headerValue = request.getHeader(REQUIRED_HEADER_NAME);
            logger.debug("Wartość nagłówka {}: {}", REQUIRED_HEADER_NAME, headerValue);

            if (headerValue == null) {
                logger.warn("Brak wymaganego nagłówka dla: {}", requestPath);

                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType("application/json");
                writeErrorResponse(response, "Brak wymaganego nagłówka " + REQUIRED_HEADER_NAME);
                return;
            } else if (!headerValue.equals(REQUIRED_HEADER_VALUE)) {
                logger.warn("Nieprawidłowa wartość nagłówka dla: {}", requestPath);

                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType("application/json");
                writeErrorResponse(response, "Nieprawidłowa wartość nagłówka " + REQUIRED_HEADER_NAME);
                return;
            }

            logger.info("Nagłówek zweryfikowany pozytywnie dla: {}", requestPath);
        }

        filterChain.doFilter(request, response);
    }

    private void writeErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
        Map<String, String> errorBody = new HashMap<>();
        errorBody.put("error", errorMessage);
        String json = objectMapper.writeValueAsString(errorBody);
        response.getWriter().write(json);
    }
}
