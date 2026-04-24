package com.report.nexoreport.repository;

import com.report.nexoreport.user.User;
import com.report.nexoreport.user.UserRole;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    List<User> findByRole(UserRole role);

    List<User> findByRoleIn(Collection<UserRole> roles);
}
