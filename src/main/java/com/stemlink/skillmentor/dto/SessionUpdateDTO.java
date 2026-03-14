package com.stemlink.skillmentor.dto;

import lombok.Data;

// Used by the admin to update a session's status or add a meeting link.
// All fields are optional — only non-null values will be applied.
@Data
public class SessionUpdateDTO {
    private String paymentStatus;   // e.g. "confirmed"
    private String sessionStatus;   // e.g. "completed"
    private String meetingLink;     // e.g. "https://meet.google.com/xyz"
}