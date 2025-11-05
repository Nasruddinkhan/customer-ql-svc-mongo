package com.alinma.customer.qlsvc.model.dto;

import java.time.LocalDateTime;

public record CustomerDto(
        String id,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDateTime createdAt
) {}