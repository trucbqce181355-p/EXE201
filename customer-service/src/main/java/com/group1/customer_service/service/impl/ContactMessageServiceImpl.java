package com.group1.customer_service.service.impl;

import com.group1.customer_service.dto.request.CreateContactMessageRequest;
import com.group1.customer_service.entity.ContactMessage;
import com.group1.customer_service.repository.ContactMessageRepository;
import com.group1.customer_service.service.ContactMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactMessageServiceImpl implements ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;

    @Override
    public ContactMessage saveContactMessage(CreateContactMessageRequest request) {
        ContactMessage message = ContactMessage.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone().trim())
                .message(request.getMessage().trim())
                .build();
        return contactMessageRepository.save(message);
    }

    @Override
    public List<ContactMessage> getAllContactMessages() {
        return contactMessageRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
