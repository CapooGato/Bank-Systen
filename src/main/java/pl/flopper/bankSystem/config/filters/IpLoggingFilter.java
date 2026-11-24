package pl.flopper.bankSystem.config.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Filtr rejestrujący i zwracający informacje o adresie IP klienta.
 *
 * Ten filtr dla endpointu /api/filter/example2 przejmuje kontrolę nad odpowiedzią
 * i zwraca szczegółowe informacje o połączeniu klienta, w tym adres IP.
 * Dla wszystkich innych endpointów jedynie loguje informacje bez modyfikowania odpowiedzi.
 */
public class IpLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(IpLoggingFilter.class);
    private static final String IP_ENDPOINT = "/api/filter/example2";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        logger.debug("IpLoggingFilter: Przetwarzanie ścieżki: {}", requestPath);

        // Pobieramy informacje o adresie IP
        String clientIp = extractClientIp(request);

        // Logujemy informacje o połączeniu dla wszystkich żądań
        logger.info("Żądanie od adresu IP: {} do ścieżki: {}", clientIp, requestPath);

        // Dla konkretnego endpointu zwracamy informacje o IP w treści odpowiedzi
        if (requestPath.equals(IP_ENDPOINT)) {
            logger.info("Generowanie raportu IP dla endpointu: {}", IP_ENDPOINT);

            // Przygotowujemy odpowiedź z informacjami o IP
            Map<String, String> ipInfo = collectIpInformation(request);

            // Ustawiamy odpowiedni typ treści
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            // Serializacja do JSON za pomocą Jackson
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(response.getWriter(), new TreeMap<>(ipInfo));

            // WAŻNE: Nie wywołujemy filterChain.doFilter(), ponieważ
            // chcemy przejąć pełną kontrolę nad odpowiedzią HTTP
            return;
        }

        // Dla innych endpointów kontynuujemy normalne przetwarzanie łańcucha filtrów
        filterChain.doFilter(request, response);
    }

    /**
     * Ekstrahuje adres IP klienta z żądania z uwzględnieniem nagłówków proxy.
     *
     * Ta metoda obsługuje różne scenariusze, w tym:
     * - Klient łączący się bezpośrednio
     * - Klient łączący się przez proxy
     * - Klient łączący się przez load balancer
     *
     * @param request Żądanie HTTP
     * @return Rzeczywisty adres IP klienta
     */
    private String extractClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");

        // Sprawdzenie różnych nagłówków używanych przez proxy
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("WL-Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("HTTP_CLIENT_IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            // Ostateczne rozwiązanie - bezpośredni adres IP
            clientIp = request.getRemoteAddr();
        }

        // Jeśli jest wiele adresów IP, bierzemy pierwszy (najbliższy klientowi)
        if (clientIp != null && clientIp.contains(",")) {
            clientIp = clientIp.split(",")[0].trim();
        }

        return clientIp;
    }

    /**
     * Zbiera szczegółowe informacje o połączeniu klienta.
     *
     * @param request Żądanie HTTP
     * @return Mapa zawierająca szczegółowe informacje o połączeniu
     */
    private Map<String, String> collectIpInformation(HttpServletRequest request) {
        Map<String, String> ipInfo = new HashMap<>();

        // Podstawowe informacje o połączeniu klienta
        ipInfo.put("clientIp", extractClientIp(request));
        ipInfo.put("remoteAddr", request.getRemoteAddr());
        ipInfo.put("remoteHost", request.getRemoteHost());
        ipInfo.put("remotePort", String.valueOf(request.getRemotePort()));

        // Informacje o serwerze
        ipInfo.put("localAddr", request.getLocalAddr());
        ipInfo.put("localName", request.getLocalName());
        ipInfo.put("localPort", String.valueOf(request.getLocalPort()));

        // Informacje o protokole
        ipInfo.put("protocol", request.getProtocol());
        ipInfo.put("scheme", request.getScheme());
        ipInfo.put("serverName", request.getServerName());
        ipInfo.put("serverPort", String.valueOf(request.getServerPort()));

        // Nagłówki związane z adresem IP i proxy
        ipInfo.put("X-Forwarded-For", request.getHeader("X-Forwarded-For") != null ?
                request.getHeader("X-Forwarded-For") : "brak");
        ipInfo.put("Proxy-Client-IP", request.getHeader("Proxy-Client-IP") != null ?
                request.getHeader("Proxy-Client-IP") : "brak");
        ipInfo.put("User-Agent", request.getHeader("User-Agent") != null ?
                request.getHeader("User-Agent") : "brak");

        return ipInfo;
    }
}