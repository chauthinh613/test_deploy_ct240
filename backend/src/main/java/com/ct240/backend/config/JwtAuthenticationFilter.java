package com.ct240.backend.config;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.ServletException;


import java.io.IOException;
import java.util.Collections;
import java.util.Date;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

            if (!signedJWT.verify(verifier)) {
                throw new RuntimeException("Invalid signature");
            }

            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();

            if (expiration == null || expiration.before(new Date())) {
                throw new RuntimeException("Token expired");
            }

            String username = signedJWT.getJWTClaimsSet().getSubject();
            String userId = signedJWT.getJWTClaimsSet().getStringClaim("userId");

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.emptyList()
                    );

            authentication.setDetails(userId);

            SecurityContextHolder.getContext().setAuthentication(authentication);

        }
        catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");

            response.getWriter().write("""
                        {
                            "code": 1003,
                            "message": "Unauthenticated"
                        }
                    """);
            System.out.println("JWT Error: " + e.getClass().getName() + ": " + e.getMessage());

            SecurityContextHolder.clearContext();

            return;
        }


        filterChain.doFilter(request, response);
    }
}