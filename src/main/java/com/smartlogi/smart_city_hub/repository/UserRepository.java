package com.smartlogi.smart_city_hub.repository;

import com.smartlogi.smart_city_hub.entity.User;
import com.smartlogi.smart_city_hub.entity.enums.Role;
import com.smartlogi.smart_city_hub.entity.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNationalId(String nationalId);

    Optional<User> findByNationalId(String nationalId);

    List<User> findByRole(Role role);

    List<User> findByStatus(UserStatus status);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    List<User> findByRoleAndStatus(Role role, UserStatus status);

    default List<User> findByActiveTrue() {
        return findByStatus(UserStatus.ACTIVE);
    }

    default List<User> findByRoleAndActiveTrue(Role role) {
        return findByRoleAndStatus(role, UserStatus.ACTIVE);
    }
}
