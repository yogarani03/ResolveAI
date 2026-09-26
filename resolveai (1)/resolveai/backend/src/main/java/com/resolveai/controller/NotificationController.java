package com.resolveai.controller;

import com.resolveai.dto.NotificationResponse;
import com.resolveai.entity.User;
import com.resolveai.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list(Authentication auth) {
        User user = currentUserProvider.get(auth);
        return ResponseEntity.ok(notificationService.getForUser(user));
    }
}
