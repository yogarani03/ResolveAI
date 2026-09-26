package com.resolveai.controller;

import com.resolveai.dto.*;
import com.resolveai.entity.User;
import com.resolveai.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<ComplaintResponse> create(@Valid @RequestBody ComplaintRequest request,
                                                      Authentication auth) {
        User user = currentUserProvider.get(auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(complaintService.createComplaint(request, user));
    }

    @GetMapping
    public ResponseEntity<List<ComplaintResponse>> list(Authentication auth) {
        User user = currentUserProvider.get(auth);
        return ResponseEntity.ok(complaintService.listForCurrentUser(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComplaintResponse> getOne(@PathVariable Long id, Authentication auth) {
        User user = currentUserProvider.get(auth);
        return ResponseEntity.ok(complaintService.getById(id, user));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ComplaintResponse> updateStatus(@PathVariable Long id,
                                                            @Valid @RequestBody StatusUpdateRequest request,
                                                            Authentication auth) {
        User user = currentUserProvider.get(auth);
        return ResponseEntity.ok(complaintService.updateStatus(id, request, user));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<ComplaintResponse> assign(@PathVariable Long id,
                                                      @Valid @RequestBody AssignRequest request,
                                                      Authentication auth) {
        User user = currentUserProvider.get(auth);
        return ResponseEntity.ok(complaintService.assignStaff(id, request, user));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<ComplaintResponse> resolve(@PathVariable Long id,
                                                       @Valid @RequestBody ResolveRequest request,
                                                       Authentication auth) {
        User user = currentUserProvider.get(auth);
        return ResponseEntity.ok(complaintService.resolve(id, request, user));
    }

    @PostMapping("/{id}/feedback")
    public ResponseEntity<Void> submitFeedback(@PathVariable Long id,
                                                @Valid @RequestBody FeedbackRequest request,
                                                Authentication auth) {
        User user = currentUserProvider.get(auth);
        complaintService.submitFeedback(id, request, user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<List<RelatedComplaintResponse>> related(@PathVariable Long id, Authentication auth) {
        User user = currentUserProvider.get(auth);
        return ResponseEntity.ok(complaintService.findRelated(id, user));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<ComplaintHistoryResponse>> history(@PathVariable Long id, Authentication auth) {
        User user = currentUserProvider.get(auth);
        return ResponseEntity.ok(complaintService.getHistory(id, user));
    }

    @GetMapping("/{id}/resolution-suggestion")
    public ResponseEntity<Map<String, String>> resolutionSuggestion(@PathVariable Long id, Authentication auth) {
        User user = currentUserProvider.get(auth);
        return ResponseEntity.ok(Map.of("suggestion", complaintService.suggestResolutionForComplaint(id, user)));
    }
}
