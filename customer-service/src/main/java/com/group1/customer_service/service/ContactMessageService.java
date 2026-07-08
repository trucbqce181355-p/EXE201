package com.group1.customer_service.service;

import com.group1.customer_service.dto.request.CreateContactMessageRequest;
import com.group1.customer_service.entity.ContactMessage;
import java.util.List;

public interface ContactMessageService {
    ContactMessage saveContactMessage(CreateContactMessageRequest request);
    List<ContactMessage> getAllContactMessages();
}
