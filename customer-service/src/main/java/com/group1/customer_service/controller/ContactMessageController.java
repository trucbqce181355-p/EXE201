package com.group1.customer_service.controller;

import com.group1.customer_service.dto.request.CreateContactMessageRequest;
import com.group1.customer_service.entity.ContactMessage;
import com.group1.customer_service.service.ContactMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    @PostMapping
    public ResponseEntity<ContactMessage> submitContactMessage(@Valid @RequestBody CreateContactMessageRequest request) {
        ContactMessage saved = contactMessageService.saveContactMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER:READ')")
    public ResponseEntity<List<ContactMessage>> getAllContactMessages() {
        List<ContactMessage> contacts = contactMessageService.getAllContactMessages();
        return ResponseEntity.ok(contacts);
    }
}
