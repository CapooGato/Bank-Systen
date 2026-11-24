package pl.flopper.bankSystem.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import pl.flopper.bankSystem.config.filters.CustomAuthenticationFilter;
import pl.flopper.bankSystem.config.filters.CustomHeaderFilter;
import pl.flopper.bankSystem.config.filters.IpLoggingFilter;

@Configuration
public class FilterConfig {

    /**
     * Konfiguruje łańcuch filtrów bezpieczeństwa dla ścieżek API filtrów.
     * Ma wyższy priorytet (niższa wartość Order) niż główny łańcuch filtrów.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain filterChainWithCustomFilters(HttpSecurity http) throws Exception {
        http
                // określamy, które ścieżki są obsługiwane przez ten łańcuch
                .securityMatcher("/api/filter/**")

                // Dodajemy niestandardowe filtry
                .addFilterBefore(
                        new CustomAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterAfter(
                        new CustomHeaderFilter(),
                        BasicAuthenticationFilter.class
                )
                .addFilterAfter(
                        new IpLoggingFilter(),
                        CustomHeaderFilter.class
                )

                // Konfiguracja autoryzacji żądań dla tych ścieżek
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/api/filter/example1").permitAll()
                        .requestMatchers("/api/filter/example2").permitAll()
                        .anyRequest().authenticated()
                )

                // Wyłączenie CSRF dla endpointów API
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}