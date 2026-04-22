package com.report.nexoreport.email;

import com.report.nexoreport.user.User;

public interface EmailService {
    void sendInvitationEmail(User user, String rawPassword);
}
