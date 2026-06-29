package com.yourcompany.roombooking.config;

import com.yourcompany.roombooking.exception.BookingException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;
import java.util.List;

/**
 * Utility class to extract claims from
 * Microsoft Entra ID JWT tokens
 */
public class JwtTokenValidator {

    public static String extractEmail(Jwt jwt) {
        if (jwt == null) {
            return "dev@company.com";
        }
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null) {
            return preferredUsername;
        }

        String email = jwt.getClaimAsString("email");
        if (email != null) {
            return email;
        }

        String upn = jwt.getClaimAsString("upn");
        if (upn != null) {
            return upn;
        }

        throw new BookingException("Unable to extract user email from token");
    }

    public static List<String> extractRoles(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null) {
            return Collections.emptyList();
        }
        return roles;
    }

    public static String extractDisplayName(Jwt jwt) {
        String name = jwt.getClaimAsString("name");
        if (name == null) {
            return "Unknown User";
        }
        return name;
    }
}
