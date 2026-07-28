package com.facthub.user.controller;

import com.facthub.common.exception.InvalidLoginException;
import com.facthub.common.response.ApiResponse;
import com.facthub.user.domain.User;
import com.facthub.user.dto.LoginRequest;
import com.facthub.user.dto.LoginResponse;
import com.facthub.user.dto.MyInfoResponse;
import com.facthub.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy
            sessionAuthenticationStrategy;
    private final UserService userService;

    private final SecurityContextLogoutHandler logoutHandler =
            new SecurityContextLogoutHandler();

    public AuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            SessionAuthenticationStrategy
                    sessionAuthenticationStrategy,
            UserService userService
    ) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository =
                securityContextRepository;
        this.sessionAuthenticationStrategy =
                sessionAuthenticationStrategy;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        try {
            Authentication authenticationRequest =
                    UsernamePasswordAuthenticationToken
                            .unauthenticated(
                                    email,
                                    request.password()
                            );

            Authentication authenticated =
                    authenticationManager.authenticate(
                            authenticationRequest
                    );

            User user =
                    userService.getActiveUserByEmail(email);

            /*
             * 기존 세션이 있으면 세션 ID를 변경해
             * 세션 고정 공격을 방지한다.
             */
            sessionAuthenticationStrategy.onAuthentication(
                    authenticated,
                    httpRequest,
                    httpResponse
            );

            SecurityContext securityContext =
                    SecurityContextHolder
                            .createEmptyContext();

            securityContext.setAuthentication(authenticated);

            SecurityContextHolder.setContext(
                    securityContext
            );

            /*
             * 세션에 SecurityContext를 명시적으로 저장한다.
             * 이 코드가 있어야 다음 요청에서도 로그인이 유지된다.
             */
            securityContextRepository.saveContext(
                    securityContext,
                    httpRequest,
                    httpResponse
            );

            return ApiResponse.success(
                    LoginResponse.from(user)
            );

        } catch (AuthenticationException exception) {
            SecurityContextHolder.clearContext();
            throw new InvalidLoginException();
        }
    }

    @GetMapping("/me")
    public ApiResponse<MyInfoResponse> me(
            Authentication authentication
    ) {
        User user = userService.getActiveUserByEmail(
                authentication.getName()
        );

        return ApiResponse.success(
                MyInfoResponse.from(user)
        );
    }

    @PostMapping("/logout")
    public ApiResponse<Map<String, String>> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        logoutHandler.logout(
                request,
                response,
                authentication
        );

        return ApiResponse.success(
                Map.of(
                        "message",
                        "로그아웃되었습니다."
                )
        );
    }
}