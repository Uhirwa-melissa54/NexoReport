package com.report.nexoreport.repository;

import com.report.nexoreport.auth.RefreshToken;
import com.report.nexoreport.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}
