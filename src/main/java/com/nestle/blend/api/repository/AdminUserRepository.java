package com.nestle.blend.api.repository;

import com.nestle.blend.api.entity.AdminUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUserEntity, UUID> {
    Optional<AdminUserEntity> findByUsernameIgnoreCase(String username);

    Optional<AdminUserEntity> findByEmailIgnoreCase(String email);
}
