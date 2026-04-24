package com.report.nexoreport.dto;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        String rawPassword =
                "Uhirwashami54.";

        String hashedPassword =
                encoder.encode(rawPassword);

        System.out.println(
                "Hashed password: "
                        + hashedPassword
        );
    }
}