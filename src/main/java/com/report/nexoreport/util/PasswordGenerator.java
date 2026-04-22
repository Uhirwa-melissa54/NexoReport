package com.report.nexoreport.util;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class PasswordGenerator {
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+[]{}";
    private static final String ALL = UPPER + LOWER + DIGITS + SPECIAL;
    private static final int DEFAULT_LENGTH = 12;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        char[] password = new char[DEFAULT_LENGTH];
        password[0] = randomChar(UPPER);
        password[1] = randomChar(LOWER);
        password[2] = randomChar(DIGITS);
        password[3] = randomChar(SPECIAL);

        for (int i = 4; i < DEFAULT_LENGTH; i++) {
            password[i] = randomChar(ALL);
        }

        for (int i = password.length - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char temp = password[i];
            password[i] = password[j];
            password[j] = temp;
        }
        return new String(password);
    }

    private char randomChar(String source) {
        return source.charAt(secureRandom.nextInt(source.length()));
    }
}
