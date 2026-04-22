package com.report.nexoreport.service;

import com.report.nexoreport.dto.ChangePasswordRequest;
import com.report.nexoreport.dto.InviteUserRequest;
import com.report.nexoreport.dto.InviteUserResult;
import com.report.nexoreport.dto.UserResponse;
import com.report.nexoreport.email.EmailService;
import com.report.nexoreport.exception.BadRequestException;
import com.report.nexoreport.exception.ResourceNotFoundException;
import com.report.nexoreport.repository.UserRepository;
import com.report.nexoreport.user.User;
import com.report.nexoreport.user.UserRole;
import com.report.nexoreport.user.UserStatus;
import com.report.nexoreport.util.PasswordGenerator;
import java.time.Instant;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final Set<UserRole> STAFF_ROLES = Set.of(
            UserRole.ADMIN,
            UserRole.TEACHER,
            UserRole.NURSE,
            UserRole.ADMINISTRATIVE_STAFF
    );
    private static final Set<UserRole> STUDENT_ROLES = Set.of(
            UserRole.CLASS_MONITOR,
            UserRole.COMMITTEE_MEMBER
    );

    private final UserRepository userRepository;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(
            UserRepository userRepository,
            PasswordGenerator passwordGenerator,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordGenerator = passwordGenerator;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public InviteUserResult inviteUser(InviteUserRequest request) {

        validateRoleSpecificFields(
                request.getRole(),
                request.getClassName(),
                request.getCommitteePosition()
        );

        User existingUser = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        // CASE 1: already invited
        if (existingUser != null && existingUser.getStatus() == UserStatus.INVITED) {

            return new InviteUserResult(
                    false,
                    "User already invited. You can resend invitation.",
                    existingUser.getId(),
                    true
            );
        }

        // CASE 2: already active
        if (existingUser != null && existingUser.getStatus() == UserStatus.ACTIVE) {

            return new InviteUserResult(
                    false,
                    "User already active",
                    existingUser.getId(),
                    false
            );
        }

        // CASE 3: new user
        String generatedPassword = passwordGenerator.generate();

        User user = new User();
        user.setFullNames(request.getFullNames());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(generatedPassword));
        user.setRole(request.getRole());
        user.setStatus(UserStatus.INVITED);
        user.setClassName(request.getClassName());
        user.setCommitteePosition(request.getCommitteePosition());

        User savedUser = userRepository.save(user);

        emailService.sendInvitationEmail(savedUser, generatedPassword);

        return new InviteUserResult(
                true,
                "Invitation sent successfully",
                savedUser.getId(),
                false
        );
    }

    @Transactional
    public void resendInvitation(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() != UserStatus.INVITED) {
            throw new BadRequestException("Cannot resend invitation for an active user");
        }

        String generatedPassword = passwordGenerator.generate();
        user.setPassword(passwordEncoder.encode(generatedPassword));
        userRepository.save(user);
        emailService.sendInvitationEmail(user, generatedPassword);
        log.info("Invitation resent for userId={} at {}", userId, Instant.now());
    }

    @Transactional
    public void updatePassword(Authentication authentication, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        validatePasswordStrength(request.getNewPassword());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public UserResponse getProfile(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
        return toUserResponse(user);
    }

    @Transactional
    public void softDeleteUser(Authentication authentication, Long userId) {
        User actor = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (actor.getId().equals(target.getId())) {
            throw new BadRequestException("You cannot delete yourself");
        }
        if (!STAFF_ROLES.contains(actor.getRole())) {
            throw new BadRequestException("Only staff users can delete accounts");
        }
        if (target.getRole() == UserRole.ADMIN) {
            throw new BadRequestException("Admin cannot delete another admin");
        }
        if (!STUDENT_ROLES.contains(target.getRole())) {
            throw new BadRequestException("Only student users can be deleted");
        }
        if (target.getStatus() == UserStatus.DEACTIVATED) {
            throw new BadRequestException("User already deactivated");
        }

        target.setStatus(UserStatus.DEACTIVATED);
        userRepository.save(target);
    }

    public boolean isStaffRole(String roleName) {
        return STAFF_ROLES.contains(UserRole.valueOf(roleName));
    }

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getClassName(),
                user.getCommitteePosition()
        );
    }

    private void validateRoleSpecificFields(UserRole role, String className, String committeePosition) {
        if (role == UserRole.CLASS_MONITOR && (className == null || className.isBlank())) {
            throw new BadRequestException("className is required for CLASS_MONITOR");
        }
        if (role == UserRole.COMMITTEE_MEMBER && (committeePosition == null || committeePosition.isBlank())) {
            throw new BadRequestException("committeePosition is required for COMMITTEE_MEMBER");
        }
    }

    private void validatePasswordStrength(String password) {
        if (password.length() < 8
                || !password.matches(".*[A-Z].*")
                || !password.matches(".*[a-z].*")
                || !password.matches(".*[0-9].*")
                || !password.matches(".*[!@#$%^&*()\\-_=+\\[\\]{}].*")) {
            throw new BadRequestException(
                    "New password must be at least 8 characters and include uppercase, lowercase, number, and special character"
            );
        }
    }
}
