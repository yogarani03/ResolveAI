package com.resolveai.service;

import com.resolveai.dto.NotificationResponse;
import com.resolveai.entity.Notification;
import com.resolveai.entity.User;
import com.resolveai.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /** Fire-and-forget in-app notification. Never throws - a notification failure
     *  must never break the complaint workflow that triggered it. */
    public void notify(User user, String message) {
        try {
            Notification n = Notification.builder()
                    .user(user)
                    .message(message)
                    .read(false)
                    .build();
            notificationRepository.save(n);
        } catch (Exception e) {
            // Swallow deliberately - notifications are a best-effort side effect.
        }
    }

    public List<NotificationResponse> getForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(NotificationResponse::from)
                .toList();
    }
}
