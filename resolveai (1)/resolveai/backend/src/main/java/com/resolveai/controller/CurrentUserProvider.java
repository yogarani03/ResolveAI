package com.resolveai.controller;

import com.resolveai.entity.User;
import com.resolveai.exception.ResourceNotFoundException;
import com.resolveai.repository.UserRepository;
import com.resolveai.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/** Small helper used by every controller to resolve the authenticated User entity
 *  (not just the JWT principal) so services can work with real JPA relationships. */
@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public User get(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
