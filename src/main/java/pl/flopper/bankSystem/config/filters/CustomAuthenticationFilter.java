package pl.flopper.bankSystem.config.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * Niestandardowy filtr uwierzytelniania obsługujący tokeny przesyłane w nagłówkach żądań.
 *
 * Filtr przetwarza każde żądanie, próbując wyekstrahować token uwierzytelniający z nagłówka.
 * Jeśli token jest prawidłowy, tworzy obiekt Authentication i zapisuje go w SecurityContextHolder,
 * umożliwiając autoryzację w dalszej części łańcucha filtrów.
 */
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        logger.debug("Przetwarzanie żądania przez CustomAuthenticationFilter");

        // Pobranie tokena uwierzytelniającego z nagłówka
        String token = extractTokenFromRequest(request);

        if (token != null) {
            logger.debug("Znaleziono token: {}", token);

            // Walidacja tokena
            if (validateToken(token)) {
                logger.info("Token prawidłowy, tworzenie obiektu uwierzytelniającego");

                // Tworzenie obiektu uwierzytelniającego na podstawie zweryfikowanego tokena
                Authentication authentication = createAuthentication(token);

                // Zapisanie uwierzytelnienia w kontekście bezpieczeństwa Spring Security
                // To kluczowy krok, który informuje Spring Security, że użytkownik jest uwierzytelniony
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.info("Uwierzytelnienie zapisane w kontekście: {}", authentication.getName());
            } else {
                logger.warn("Nieprawidłowy token: {}", token);
                // Opcjonalnie, można wyczyścić kontekst bezpieczeństwa
                SecurityContextHolder.clearContext();
            }
        } else {
            logger.debug("Brak tokena w żądaniu");
        }

        // Kontynuacja łańcucha filtrów
        // Ten krok jest krytyczny - bez niego żądanie nie będzie dalej przetwarzane
        filterChain.doFilter(request, response);
    }

    /**
     * Ekstrahuje token z nagłówka Authorization.
     *
     * Szuka nagłówka w formacie "Bearer {token}" i wyodrębnia część tokenową.
     *
     * @param request Żądanie HTTP
     * @return Wyodrębniony token lub null, jeśli nagłówek nie istnieje lub ma nieprawidłowy format
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        logger.debug("Nagłówek Authorization: {}", bearerToken);

        // Sprawdzenie czy nagłówek istnieje i ma prawidłowy format
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            // Wyodrębnienie tokena (usunięcie prefixu "Bearer ")
            return bearerToken.substring(7);
        }

        return null;
    }

    /**
     * Waliduje podany token.
     *
     * W rzeczywistej implementacji należy zweryfikować podpis tokena, datę ważności, itp.
     * Ta implementacja jest uproszczona i zawsze zwraca true.
     *
     * @param token Token do walidacji
     * @return true jeśli token jest prawidłowy, false w przeciwnym razie
     */
    private boolean validateToken(String token) {
        // Uproszczona implementacja - w praktyce należy:
        // 1. Zweryfikować podpis cyfrowy tokena
        // 2. Sprawdzić czy token nie wygasł
        // 3. Sprawdzić czy token nie został odwołany
        // 4. Zweryfikować czy token jest przeznaczony dla tej aplikacji

        // Dla demonstracji akceptujemy przykładowy token
        if ("example-valid-token".equals(token)) {
            return true;
        }

        // Tutaj można dodać bardziej zaawansowaną logikę walidacji

        return false; // W tej implementacji akceptujemy tylko jeden konkretny token
    }

    /**
     * Tworzy obiekt Authentication na podstawie tokena.
     *
     * W rzeczywistej implementacji należy zdekodować token i wydobyć z niego
     * informacje o użytkowniku i jego uprawnieniach.
     *
     * @param token Zwalidowany token
     * @return Obiekt Authentication reprezentujący uwierzytelnionego użytkownika
     */
    private Authentication createAuthentication(String token) {
        // Uproszczona implementacja - w praktyce:
        // 1. Zdekodować payload tokena
        // 2. Wydobyć informacje o użytkowniku (ID, nazwa, role)
        // 3. Zweryfikować czy użytkownik istnieje w systemie

        // Dla demonstracji tworzymy hardcoded Authentication
        return new UsernamePasswordAuthenticationToken(
                "tokenUser", // principal (nazwa użytkownika)
                null, // credentials (nie potrzebne po uwierzytelnieniu)
                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")) // uprawnienia
        );
    }
}