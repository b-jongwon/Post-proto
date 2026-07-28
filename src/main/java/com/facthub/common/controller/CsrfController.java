package com.facthub.common.controller;

import com.facthub.common.response.ApiResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {

    @GetMapping("/api/csrf")
    public ApiResponse<CsrfResponse> csrf(
            CsrfToken csrfToken
    ) {
        CsrfResponse response = new CsrfResponse(
                csrfToken.getHeaderName(),
                csrfToken.getParameterName(),
                csrfToken.getToken()
        );

        return ApiResponse.success(response);
    }

    public record CsrfResponse(
            String headerName,
            String parameterName,
            String token
    ) {
    }
}