package com.mackessels.fittrackbackend.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticatedUserService {

    public Long getUserId(Jwt jwt) {
        try {
            long userId = Long.parseLong(jwt.getSubject());

            if (userId <= 0) {
                throw new NumberFormatException();
            }

            return userId;
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid token subject"
            );
        }
    }

    public Long requireOwnUser(Long requestedId, Jwt jwt) {
        Long authenticatedId = getUserId(jwt);

        if (!authenticatedId.equals(requestedId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Resource not found"
            );
        }

        return authenticatedId;
    }
}