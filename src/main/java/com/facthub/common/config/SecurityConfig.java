package com.facthub.common.config;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.facthub.user.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            DaoAuthenticationProvider authenticationProvider
    ) {
        return new ProviderManager(
                List.of(authenticationProvider)
        );
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        );
    }

    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new ChangeSessionIdAuthenticationStrategy();
    }

    @Bean
    public UrlBasedCorsConfigurationSource
    corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        /*
         * React 개발 서버 주소
         *
         * 배포할 때는 실제 프론트엔드 도메인으로 변경한다.
         */
        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:5173"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Content-Type",
                        "Accept",
                        "X-XSRF-TOKEN",
                        "X-CSRF-TOKEN"
                )
        );

        /*
         * JSESSIONID 같은 세션 쿠키를
         * 브라우저가 함께 전송하도록 허용한다.
         */
        configuration.setAllowCredentials(true);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/api/**",
                configuration
        );

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository,
            UrlBasedCorsConfigurationSource
                    corsConfigurationSource
    ) throws Exception {

        CookieCsrfTokenRepository csrfTokenRepository =
                new CookieCsrfTokenRepository();

        http
                .cors(cors -> cors
                        .configurationSource(
                                corsConfigurationSource
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // 로그인 없이 접근 가능한 공통 API
                        .requestMatchers(
                                "/api/health",
                                "/api/csrf",
                                "/api/auth/signup",
                                "/api/auth/login",
                                "/api/auth/email-verifications",
                                "/api/auth/email-verifications/confirm",
                                "/actuator/health",
                                "/actuator/info",
                                "/error"
                        ).permitAll()

                        // 게시글 조회는 비회원도 가능
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/posts",
                                "/api/posts/**"
                        ).permitAll()

                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMIN")

                        // 나머지 요청은 로그인 필요
                        .anyRequest().authenticated()
                )

                .securityContext(context -> context
                        .requireExplicitSave(true)
                        .securityContextRepository(
                                securityContextRepository
                        )
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                /*
                 * 회원가입과 로그인은 로그인 전 요청이므로
                 * CSRF 검사를 제외한다.
                 *
                 * 로그아웃, 게시글 작성·수정·삭제는
                 * CSRF 토큰이 반드시 필요하다.
                 */
                .csrf(csrf -> csrf
                        .csrfTokenRepository(
                                csrfTokenRepository
                        )
                        .ignoringRequestMatchers(
                                "/api/auth/signup",
                                "/api/auth/login",
                                "/api/auth/email-verifications",
                                "/api/auth/email-verifications/confirm"
                        )
                )

                .requestCache(cache -> cache.disable())

                .exceptionHandling(exception -> exception

                        // 로그인하지 않은 사용자가 보호된 API 접근
                        .authenticationEntryPoint(
                                (request, response, authException) -> {
                                    response.setStatus(401);
                                    response.setContentType(
                                            "application/json"
                                    );
                                    response.setCharacterEncoding(
                                            "UTF-8"
                                    );

                                    response.getWriter().write("""
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "code": "UNAUTHORIZED",
                                                "message": "로그인이 필요합니다.",
                                                "fields": {}
                                              }
                                            }
                                            """);
                                }
                        )

                        // 로그인했지만 권한이 없는 경우
                        .accessDeniedHandler(
                                (request, response, deniedException) -> {
                                    response.setStatus(403);
                                    response.setContentType(
                                            "application/json"
                                    );
                                    response.setCharacterEncoding(
                                            "UTF-8"
                                    );

                                    response.getWriter().write("""
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "code": "ACCESS_DENIED",
                                                "message": "접근 권한이 없습니다.",
                                                "fields": {}
                                              }
                                            }
                                            """);
                                }
                        )
                )

                // 기본 HTML 로그인 화면 사용 안 함
                .formLogin(form -> form.disable())

                // HTTP Basic 인증 사용 안 함
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
