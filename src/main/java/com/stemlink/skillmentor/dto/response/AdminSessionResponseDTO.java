package com.stemlink.skillmentor.dto.response;

import lombok.Data;
import java.util.Date;

// DTO returned to the admin dashboard for the "Manage Bookings" table.
// Includes student info that the regular student-facing DTO omits.
@Data
public class AdminSessionResponseDTO {
    private Integer id;
    private String studentName;
    private String studentEmail;
    private String mentorName;
    private String subjectName;
    private Date sessionAt;
    private Integer durationMinutes;
    private String sessionStatus;
    private String paymentStatus;
    private String meetingLink;
}